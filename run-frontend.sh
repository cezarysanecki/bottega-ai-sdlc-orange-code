#!/usr/bin/env bash
#
# Healthcare Demo - Frontend Dev Server
# Starts Vite dev server with hot reload
# Requires: Node.js 18+
#
set -e

echo ""
echo "=============================================="
echo "   Healthcare Demo - Frontend"
echo "=============================================="
echo ""

# Check for Node.js
if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js is not installed."
    echo ""
    echo "Please install Node.js 18 or later from one of these sources:"
    echo "  - https://nodejs.org/ (recommended)"
    echo "  - Using nvm: https://github.com/nvm-sh/nvm"
    echo ""
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "ERROR: Node.js 18+ required (found Node.js $NODE_VERSION)"
    echo "Please upgrade Node.js from https://nodejs.org/"
    exit 1
fi

echo "Node.js version: $(node -v)"
echo ""

# Change to the frontend directory
cd "$(dirname "$0")/frontend/med-frontend"

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
    echo ""
fi

echo "Starting frontend dev server..."
echo "UI will be available at: http://localhost:5173"
echo ""

# Start Vite dev server
npm run dev
