#!/bin/bash
set -x

DIR="$(dirname $( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd ))"
mvn -f "$DIR/queryx-fn" clean install

export TIMESTAMP=$(date +%Y%m%d%H%M%S)
export VERSION="$(cat "$DIR/VERSION.txt").$TIMESTAMP"
mkdir -p dist
cp "$DIR/queryx-fn/target/function.zip" "dist/"
cp "$DIR/queryx-fn/template.sam.yaml" "dist/"

# Create a new release using the GitHub CLI
DRAFT=${DRAFT:-"--draft"}
echo "Creating new GitHub release for tag $VERSION..."
gh release create "$VERSION" --title "Release $VERSION" --notes "New release $VERSION" ./dist/* $DRAFT

# Check for successful release creation
if [ $? -eq 0 ]; then
    echo "Release created successfully."
else
    echo "Failed to create release."
    exit 1
fi

echo "Version $VERSION released"
