
set -e

function detect_os() {
  case "$(uname -s)" in
      Linux*)   echo "Linux" ;;
      Darwin*)  echo "macOS" ;;
      MINGW*|MSYS*|CYGWIN*) echo "Windows" ;;
      *)        echo "Unknown" ;;
  esac
}

OS_NAME=$(detect_os)
echo "Detected OS: $OS_NAME"

if command -v docker &> /dev/null; then
    echo "✅ Docker is already installed. Consider updating it if needed."
else
    echo "❌ Docker is not installed."

    case "$OS_NAME" in
        "Linux")
            echo "👉 Please install Docker using your package manager."
            echo "For example (Ubuntu/Debian):"
            echo "    sudo apt update && sudo apt install -y docker.io"
            ;;
        "macOS")
            echo "👉 Please install Docker Desktop from the official website:"
            echo "    https://docs.docker.com/desktop/setup/install/mac-install/"
            ;;
        "Windows")
            echo "👉 Downloading Docker Desktop installer for Windows..."
            curl -L -o DockerInstaller.exe "https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe"

            echo "📦 Running Docker Desktop installer..."
            cmd.exe /c start DockerInstaller.exe

            echo "🧹 Cleaning up installer..."

            rm DockerInstaller.exe
            ;;
        *)
            echo "⚠️ Unsupported or unrecognized OS. Please install Docker manually from:"
            echo "    https://docs.docker.com/desktop/"
            ;;
      esac

fi
