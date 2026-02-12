#!/usr/bin/env bash
set -euo pipefail

# Initialise git repo and create recommended branches

if [ ! -d .git ]; then
  git init
fi

git checkout -b main || true
git checkout -b develop || true

# Backend feature branches
git branch backend/feature-auth || true
git branch backend/feature-events || true
git branch backend/feature-orders-payments || true
git branch backend/feature-tickets-qr || true
git branch backend/feature-notifications || true

# Frontend feature branches
git branch frontend/feature-auth || true
git branch frontend/feature-events || true
git branch frontend/feature-checkout || true
git branch frontend/feature-dashboards || true

# Release templates
git branch backend/release-x.y || true
git branch frontend/release-x.y || true

echo "Branches initialisées."