package com.mediguide.firstaid.voice

import android.content.Context
import android.util.Log
import com.mediguide.firstaid.BuildConfig
import com.mediguide.firstaid.data.voice.VoiceAction
import com.mediguide.firstaid.data.voice.VoiceCommandType
import com.mediguide.firstaid.data.voice.VoiceResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gemini Live API Manager for real-time voice interaction in emergency first aid scenarios
 * Provides bidirectional audio streaming with function calling capabilities
 */
class GeminiAIManager(private val context: Context) {

    private val tag = "GeminiLiveAIManager"

    // Model readiness tracking
    private var isAIModelReady: Boolean = false
    val isModelInitialized = AtomicBoolean(false)

    // Google AI Client model instance
    private var generativeModel: GenerativeModel? = null

    // Training reference procedures
    private val emergencyProcedures = mutableListOf<String>()

    // Callback for function calls from AI
    var onEmergencyCall: ((String, String) -> Unit)? = null
    var onNavigateToProcedure: ((String) -> Unit)? = null

    private val emergencyResponses = mapOf(
        "cpr" to VoiceResponse(
            text = "Training reference: Place hands on center of chest. Push hard and fast at least 2 inches deep, 100-120 compressions per minute. Call 112 in real emergencies!",
            actionRequired = VoiceAction.CallEmergency("112"),
            metadata = mapOf("urgency" to "HIGH", "procedure" to "CPR")
        ),
        "choking" to VoiceResponse(
            text = "Training reference for choking: Stand behind person, place hands below ribcage, thrust upward and inward 5 times. Repeat until object comes out or person becomes unconscious.",
            actionRequired = VoiceAction.NavigateToProcedure("choking"),
            metadata = mapOf("urgency" to "HIGH", "procedure" to "HEIMLICH")
        ),
        "bleeding" to VoiceResponse(
            text = "Training reference: Apply direct pressure with clean cloth. Keep pressure constant. Elevate the injured area above heart level if possible.",
            actionRequired = VoiceAction.NavigateToProcedure("bleeding"),
            metadata = mapOf("urgency" to "HIGH", "procedure" to "BLEEDING_CONTROL")
        ),
        "burns" to VoiceResponse(
            text = "Cool burn with clean, cool water for 10-20 minutes. Do not use ice. Remove jewelry before swelling. Cover with sterile bandage.",
            actionRequired = VoiceAction.NavigateToProcedure("burns"),
            metadata = mapOf("urgency" to "MEDIUM", "procedure" to "BURN_CARE")
        ),
        "heart_attack" to VoiceResponse(
            text = "Training reference: Call 112 immediately! Have person sit down and rest. Give aspirin if available and not allergic. Monitor breathing and pulse.",
            actionRequired = VoiceAction.CallEmergency("112"),
            metadata = mapOf("urgency" to "HIGH", "procedure" to "HEART_ATTACK")
        ),
        "stroke" to VoiceResponse(
            text = "Training reference: Call 112 now! Note time symptoms started. Keep person comfortable, lying down. Do not give food, drink, or medication.",
            actionRequired = VoiceAction.CallEmergency("112"),
            metadata = mapOf("urgency" to "HIGH", "procedure" to "STROKE")
        ),
        "fracture" to VoiceResponse(
            text = "Training reference: Don't move injured area. Immobilize with splint using rigid material. Apply ice wrapped in cloth. Seek help if needed.",
            actionRequired = VoiceAction.NavigateToProcedure("fractures"),
            metadata = mapOf("urgency" to "MEDIUM", "procedure" to "FRACTURE_CARE")
        )
    )

    init {
        initializeLiveModel()
    }

    /**
     * Initialize the Gemini model with emergency first aid system instructions
     */
    private fun initializeLiveModel() {
        try {
            val apiKey = getGeminiApiKey()
            if (apiKey.isBlank()) {
                Log.w(tag, "Cannot initialize Gemini model: API key is missing or empty")
                isModelInitialized.set(false)
                return
            }

            // Initialize Google AI Client GenerativeModel
            // Using gemini-2.0-flash for latest model capabilities
            generativeModel = GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                }
            )

            isAIModelReady = true
            isModelInitialized.set(true)
            Log.d(tag, "Gemini model initialized successfully for first aid training")
        } catch (e: Exception) {
            Log.w(tag, "Failed to initialize Gemini model, falling back to offline mode", e)
            isModelInitialized.set(false)
        }
    }

    /**
     * Get the Gemini API key from BuildConfig or environment
     */
    private fun getGeminiApiKey(): String {
        return try {
            Log.d(tag, "Attempting to load API key from BuildConfig...")

            // First try to get from BuildConfig
            val buildConfigClass = Class.forName("${context.packageName}.BuildConfig")
            Log.d(tag, "BuildConfig class loaded: ${buildConfigClass.name}")

            val field = buildConfigClass.getDeclaredField("GEMINI_API_KEY")
            Log.d(tag, "GEMINI_API_KEY field found: ${field.name}")

            val apiKey = field.get(null) as? String ?: ""
            Log.d(tag, "API key retrieved. Length: ${apiKey.length}, Is blank: ${apiKey.isBlank()}")

            if (apiKey.isBlank()) {
                Log.w(tag, "API key is blank or empty - check your local.properties file and rebuild the app")
                return ""
            }

            Log.d(tag, "API key loaded successfully from BuildConfig (${apiKey.take(10)}...)")
            return apiKey
        } catch (e: NoSuchFieldException) {
            Log.w(tag, "GEMINI_API_KEY field not found in BuildConfig - check build.gradle.kts configuration", e)
            ""
        } catch (e: ClassNotFoundException) {
            Log.w(tag, "BuildConfig class not found - this should not happen", e)
            ""
        } catch (e: Exception) {
            Log.w(tag, "Could not retrieve API key from BuildConfig", e)
            ""
        }
    }


    /**
     * Process voice input with Gemini AI for training purposes
     */
    suspend fun processEmergencyVoiceInput(text: String): Result<VoiceResponse> = withContext(Dispatchers.IO) {
        try {
            if (!isModelInitialized.get() || generativeModel == null) {
                return@withContext processFallbackCommand(text)
            }

            val trainingPrompt = """
                ${getEmergencyFirstAidPrompt()}
                
                TRAINING SCENARIO: $text
                
                Analyze this scenario and provide reference first aid guidance for educational purposes.
                Provide clear, step-by-step training instructions.
            """.trimIndent()

            // Use Google AI Client to generate content
            val response = generativeModel!!.generateContent(
                content {
                    text(trainingPrompt)
                }
            )

            val responseText = response.text ?: getBasicEmergencyResponse(text)

            // Analyze for emergency actions needed
            val actionRequired = when {
                responseText.contains("call 112", ignoreCase = true) ||
                responseText.contains("call emergency", ignoreCase = true) -> {
                    onEmergencyCall?.invoke("medical", "HIGH")
                    VoiceAction.CallEmergency("112")
                }
                responseText.contains("navigate to", ignoreCase = true) -> {
                    val procedureName = extractProcedureName(text)
                    onNavigateToProcedure?.invoke(procedureName)
                    VoiceAction.NavigateToProcedure(procedureName)
                }
                else -> null
            }

            // Determine urgency level
            val urgency = when {
                responseText.contains("immediately", ignoreCase = true) ||
                responseText.contains("112", ignoreCase = true) -> "CRITICAL"
                responseText.contains("urgent", ignoreCase = true) ||
                responseText.contains("quickly", ignoreCase = true) -> "HIGH"
                else -> "MEDIUM"
            }

            Result.success(VoiceResponse(
                text = responseText,
                actionRequired = actionRequired,
                metadata = mapOf(
                    "source" to "gemini_ai",
                    "urgency" to urgency,
                    "timestamp" to System.currentTimeMillis().toString()
                )
            ))
        } catch (e: Exception) {
            Log.e(tag, "Error processing emergency voice input with AI", e)
            processFallbackCommand(text)
        }
    }

    /**
     * Extract procedure name from user input for navigation
     */
    private fun extractProcedureName(text: String): String {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("cpr") || lowerText.contains("cardiac") -> "cpr"
            lowerText.contains("choking") || lowerText.contains("heimlich") -> "choking"
            lowerText.contains("bleeding") || lowerText.contains("blood") -> "bleeding"
            lowerText.contains("burn") -> "burns"
            lowerText.contains("fracture") || lowerText.contains("broken") -> "fractures"
            lowerText.contains("heart attack") -> "heart_attack"
            lowerText.contains("stroke") -> "stroke"
            else -> "general_emergency"
        }
    }

    /**
     * Get the emergency first aid system prompt for the AI
     */
    private fun getEmergencyFirstAidPrompt(): String {
        return """
            You are an AI First Aid Training Assistant. Your role is to provide IMMEDIATE, SPECIFIC educational guidance based on the situation described.
            
            IMPORTANT DISCLAIMER: This is for educational reference only. In real emergencies, always call emergency services (112/911).

            CRITICAL INSTRUCTION: Analyze the specific situation first, then provide ONLY the most relevant, immediate reference actions for that specific case. DO NOT provide generic checklists unless specifically needed.

            RESPONSE APPROACH:
            1. ANALYZE THE SPECIFIC SITUATION: What exactly is happening? What are the key symptoms/conditions mentioned?
            2. DETERMINE IMMEDIATE PRIORITY: What is the most urgent reference action needed right now?
            3. PROVIDE TARGETED GUIDANCE: Give specific training steps for THIS situation
            4. BE CONCISE BUT COMPLETE: Focus on what matters most for this specific scenario

            RESPONSE STRUCTURE:
            - IMMEDIATE ACTION (1-2 sentences): What to do RIGHT NOW (educational reference)
            - SPECIFIC STEPS (3-5 steps max): Targeted to this exact situation  
            - WHEN TO CALL FOR HELP: Only mention if relevant for this specific case
            - MONITOR FOR: What to watch for specifically in this situation

            EXAMPLES OF GOOD RESPONSES:
            - If "unconscious": "Check for breathing immediately. If not breathing, start CPR training steps - 30 chest compressions, 2 breaths."
            - If "choking": "Stand behind them and give 5 firm back blows between shoulder blades. If object doesn't come out, do abdominal thrusts."
            - If "chest pain": "Have them sit down and rest immediately. Call for help now. Give aspirin if available and no allergies."

            AVOID:
            - Medical diagnosis language
            - Clinical terminology
            - Long comprehensive lists when situation is specific
            - Generic emergency overviews

            TONE: Calm, educational, focused. Provide training reference information for learning purposes.

            Remember: This is educational content. Provide learning reference for the situation, always remind to call emergency services in real situations.
        """
    }

    /**
     * Check if the AI model is available
     */
    fun isServiceAvailable(): Boolean {
        return isModelInitialized.get()
    }

    /**
     * Process voice commands (legacy compatibility method)
     */
    suspend fun processVoiceCommand(text: String): Result<VoiceResponse> {
        return processEmergencyVoiceInput(text)
    }


    /**
     * Fallback command processing when AI is unavailable
     */
    private suspend fun processFallbackCommand(command: String): Result<VoiceResponse> {
        return try {
            val lowerCommand = command.lowercase()

            // Check for known emergency keywords and provide basic responses
            val response = when {
                lowerCommand.contains("cpr") || lowerCommand.contains("cardiac") -> {
                    emergencyResponses["cpr"]!!
                }
                lowerCommand.contains("choking") || lowerCommand.contains("heimlich") -> {
                    emergencyResponses["choking"]!!
                }
                lowerCommand.contains("bleeding") || lowerCommand.contains("blood") -> {
                    emergencyResponses["bleeding"]!!
                }
                lowerCommand.contains("burn") -> {
                    emergencyResponses["burns"]!!
                }
                lowerCommand.contains("heart attack") -> {
                    emergencyResponses["heart_attack"]!!
                }
                lowerCommand.contains("stroke") -> {
                    emergencyResponses["stroke"]!!
                }
                lowerCommand.contains("fracture") || lowerCommand.contains("broken") -> {
                    emergencyResponses["fracture"]!!
                }
                else -> {
                    VoiceResponse(
                        text = "I'm operating in offline mode. For educational reference only - call 112 in real emergencies. Can you describe your training scenario?",
                        metadata = mapOf(
                            "source" to "offline_mode",
                            "urgency" to "MEDIUM",
                            "timestamp" to System.currentTimeMillis().toString()
                        )
                    )
                }
            }

            Result.success(response)
        } catch (e: Exception) {
            Log.e(tag, "Error in fallback command processing", e)
            Result.failure(e)
        }
    }

    /**
     * Get basic training response when AI processing fails
     */
    private fun getBasicEmergencyResponse(input: String): String {
        val lowerInput = input.lowercase()
        return when {
            lowerInput.contains("cpr") || lowerInput.contains("cardiac") -> {
                "Training reference: Call 112 immediately. Start CPR: 30 chest compressions, 2 rescue breaths. Push hard and fast on center of chest."
            }
            lowerInput.contains("choking") -> {
                "Training reference: For choking - 5 back blows between shoulder blades, then 5 abdominal thrusts. Repeat until object comes out."
            }
            lowerInput.contains("bleeding") -> {
                "Training reference: Control bleeding by applying direct pressure with clean cloth. Keep pressure constant. Elevate above heart if possible."
            }
            lowerInput.contains("burn") -> {
                "Training reference: Cool burn with clean, cool water for 10-20 minutes. Do not use ice. Cover with sterile bandage."
            }
            lowerInput.contains("heart attack") -> {
                "Training reference: Call 112! Have person sit and rest. Give aspirin if available and not allergic. Monitor breathing."
            }
            lowerInput.contains("stroke") -> {
                "Training reference: Call 112 immediately! Note time symptoms started. Keep person lying down. Do not give food or drink."
            }
            else -> {
                "This is a training scenario. For real situations, call 112 immediately. Can you provide more specific details?"
            }
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            isModelInitialized.set(false)
            generativeModel = null
            Log.d(tag, "GeminiAIManager cleaned up")
        } catch (e: Exception) {
            Log.e(tag, "Error during cleanup", e)
        }
    }
}
