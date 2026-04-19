@echo off
powershell -ExecutionPolicy Bypass -File "%~dp0sync-version.ps1" %*
