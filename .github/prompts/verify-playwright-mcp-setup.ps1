# Playwright MCP Installation Verification Script
# Run this script to verify all prerequisites and configurations are in place

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Playwright MCP + GitHub Copilot Verification Script     ║" -ForegroundColor Cyan
Write-Host "║   For Android Studio Configuration                        ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

Write-Host ""

# Track overall status
$allPassed = $true

# Function to print status
function PrintStatus {
    param(
        [string]$Component,
        [bool]$Status,
        [string]$Details
    )
    
    if ($Status) {
        Write-Host "✅ $Component" -ForegroundColor Green
        if ($Details) {
            Write-Host "   └─ $Details" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ $Component" -ForegroundColor Red
        if ($Details) {
            Write-Host "   └─ $Details" -ForegroundColor Yellow
        }
        $script:allPassed = $false
    }
    Write-Host ""
}

# ============================================
# 1. Check Node.js Installation
# ============================================
Write-Host "1️⃣  CHECKING NODE.JS & NPM..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────" -ForegroundColor Gray

try {
    $nodeVersion = node --version
    $nodeCheck = $true
    $nodeDetails = "Installed: $nodeVersion"
} catch {
    $nodeCheck = $false
    $nodeDetails = "Not found in PATH. Install from https://nodejs.org/"
}
PrintStatus "Node.js" $nodeCheck $nodeDetails

# Check Node.js version (must be 18+)
if ($nodeCheck) {
    $nodeVersionNumber = [int]($nodeVersion -replace 'v(\d+)\..*', '$1')
    if ($nodeVersionNumber -ge 18) {
        PrintStatus "Node.js Version Check" $true "Version $nodeVersion meets requirement (18+)"
    } else {
        PrintStatus "Node.js Version Check" $false "Version $nodeVersion is below required version 18+"
    }
}

try {
    $npmVersion = npm --version
    $npmCheck = $true
    $npmDetails = "Installed: npm v$npmVersion"
} catch {
    $npmCheck = $false
    $npmDetails = "Not found. npm comes with Node.js"
}
PrintStatus "npm" $npmCheck $npmDetails

# ============================================
# 2. Check npx Availability
# ============================================
Write-Host "2️⃣  CHECKING NPX COMMAND..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────" -ForegroundColor Gray

try {
    $npxVersion = npx --version
    $npxCheck = $true
    $npxDetails = "Available: npx v$npxVersion"
} catch {
    $npxCheck = $false
    $npxDetails = "Not available. Reinstall Node.js or check PATH"
}
PrintStatus "npx CLI" $npxCheck $npxDetails

# ============================================
# 3. Check Android Studio Configuration Directory
# ============================================
Write-Host "3️⃣  CHECKING ANDROID STUDIO CONFIGURATION..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────" -ForegroundColor Gray

$configPath = "$env:APPDATA\Google"
$androidStudioDir = Get-ChildItem -Path $configPath -Directory -ErrorAction SilentlyContinue | 
    Where-Object { $_.Name -match "AndroidStudio" } | 
    Select-Object -First 1

if ($androidStudioDir) {
    $configDir = $androidStudioDir.FullName
    PrintStatus "Android Studio Config Found" $true "Located at: $configDir"
    
    # Check for mcp.json file
    $mcpJsonPath = Join-Path $configDir "mcp.json"
    if (Test-Path $mcpJsonPath) {
        Write-Host "✅ mcp.json Configuration File" -ForegroundColor Green
        Write-Host "   └─ Found at: $mcpJsonPath" -ForegroundColor Gray
        
        # Validate JSON syntax
        try {
            $mcpContent = Get-Content $mcpJsonPath -Raw
            $mcpJson = $mcpContent | ConvertFrom-Json
            Write-Host "✅ JSON Syntax" -ForegroundColor Green
            Write-Host "   └─ Valid JSON structure" -ForegroundColor Gray
            
            # Check if playwright server is configured
            if ($mcpJson.mcpServers.playwright) {
                Write-Host "✅ Playwright MCP Server" -ForegroundColor Green
                Write-Host "   └─ Configured in mcp.json" -ForegroundColor Gray
            } else {
                Write-Host "❌ Playwright MCP Server" -ForegroundColor Red
                Write-Host "   └─ Not found in mcp.json. Please add Playwright configuration." -ForegroundColor Yellow
                $allPassed = $false
            }
        } catch {
            Write-Host "❌ JSON Syntax" -ForegroundColor Red
            Write-Host "   └─ Invalid JSON in mcp.json: $_" -ForegroundColor Yellow
            $allPassed = $false
        }
    } else {
        Write-Host "⚠️  mcp.json Configuration File" -ForegroundColor Yellow
        Write-Host "   └─ Not found at: $mcpJsonPath" -ForegroundColor Gray
        Write-Host "   └─ ACTION: Create mcp.json file in Android Studio config directory" -ForegroundColor Yellow
        $allPassed = $false
    }
} else {
    Write-Host "❌ Android Studio Configuration Directory" -ForegroundColor Red
    Write-Host "   └─ Not found. Is Android Studio installed?" -ForegroundColor Yellow
    $allPassed = $false
}

Write-Host ""

# ============================================
# 4. Check Playwright MCP Package
# ============================================
Write-Host "4️⃣  CHECKING PLAYWRIGHT MCP PACKAGE..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────" -ForegroundColor Gray

if ($npmCheck -and $npxCheck) {
    Write-Host "⏳ Testing Playwright MCP package (this may take a moment)..." -ForegroundColor Cyan
    try {
        # Test if playwright MCP can be downloaded/run
        $playwrightTest = & npx -y @playwright/mcp@latest --version 2>&1 | Select-Object -First 1
        
        if ($LASTEXITCODE -eq 0) {
            PrintStatus "@playwright/mcp Package" $true "Package is accessible and functional"
        } else {
            PrintStatus "@playwright/mcp Package" $false "Package exists but may have issues: $playwrightTest"
        }
    } catch {
        PrintStatus "@playwright/mcp Package" $false "Unable to test package: $_"
    }
}

Write-Host ""

# ============================================
# 5. Check Playwright Browsers Installation
# ============================================
Write-Host "5️⃣  CHECKING PLAYWRIGHT BROWSERS..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────" -ForegroundColor Gray

# Check if browsers are installed in user's cache
$playwrightCache = "$env:USERPROFILE\.cache\ms-playwright"
if (Test-Path $playwrightCache) {
    $browsers = Get-ChildItem $playwrightCache -Directory -ErrorAction SilentlyContinue
    if ($browsers.Count -gt 0) {
        PrintStatus "Playwright Browsers" $true "Found $($browsers.Count) browser(s) installed"
    } else {
        Write-Host "⚠️  Playwright Browsers" -ForegroundColor Yellow
        Write-Host "   └─ No browsers installed yet. They will download on first use (~170MB)" -ForegroundColor Gray
    }
} else {
    Write-Host "⚠️  Playwright Browsers" -ForegroundColor Yellow
    Write-Host "   └─ Cache directory not found. Browsers will download on first use (~170MB)" -ForegroundColor Gray
}

Write-Host ""

# ============================================
# 6. System Checks
# ============================================
Write-Host "6️⃣  SYSTEM CHECKS..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────" -ForegroundColor Gray

# Check available disk space
try {
    $drive = Get-Volume | Where-Object { $_.DriveLetter -eq $env:SYSTEMDRIVE[0] }
    $freeSpace = $drive.SizeRemaining / 1GB
    
    if ($freeSpace -gt 2) {
        PrintStatus "Disk Space" $true "Available: $([Math]::Round($freeSpace, 2)) GB (Recommended: 2+ GB)"
    } else {
        PrintStatus "Disk Space" $false "Available: $([Math]::Round($freeSpace, 2)) GB (Minimum 2 GB recommended)"
    }
} catch {
    Write-Host "⚠️  Disk Space Check" -ForegroundColor Yellow
    Write-Host "   └─ Could not determine. Ensure at least 2GB free space." -ForegroundColor Gray
}

# Check PATH variable includes Node.js
$pathContainsNodejs = $env:PATH -split ";" | Where-Object { $_ -match "nodejs" }
if ($pathContainsNodejs) {
    PrintStatus "Node.js in PATH" $true "Found in system PATH"
} else {
    Write-Host "⚠️  Node.js in PATH" -ForegroundColor Yellow
    Write-Host "   └─ Verify Node.js installation directory is in PATH environment variable" -ForegroundColor Gray
}

Write-Host ""

# ============================================
# 7. Summary
# ============================================
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
if ($allPassed) {
    Write-Host "║                   ✅ ALL CHECKS PASSED!                  ║" -ForegroundColor Green
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "You're ready to use Playwright MCP with GitHub Copilot!" -ForegroundColor Green
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Restart Android Studio completely" -ForegroundColor Gray
    Write-Host "2. Open Copilot Chat (Alt + 2)" -ForegroundColor Gray
    Write-Host "3. Type: 'What MCP servers are available?'" -ForegroundColor Gray
} else {
    Write-Host "║              ⚠️  SOME CHECKS FAILED OR WARNING            ║" -ForegroundColor Yellow
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Please address the issues marked with ❌ above." -ForegroundColor Yellow
    Write-Host "Refer to the troubleshooting guide for solutions." -ForegroundColor Gray
}

Write-Host ""
Write-Host "═════════════════════════════════════════════════════════════" -ForegroundColor Gray
