@echo off
REM OneDriveBackup for FinancialDBReporting data files

if not exist "%userprofile%\OneDrive\Documents\App_Data_and_Reporting_Backups\FinancialDBReporting\" mkdir %userprofile%\OneDrive\Documents\App_Data_and_Reporting_Backups\FinancialDBReporting

xcopy out\*.txt %userprofile%\OneDrive\Documents\App_Data_and_Reporting_Backups\FinancialDBReporting\out /I /Y
