#!/usr/bin/env node

var packageJson = require('./package.json')
var execSync = require('child_process').execSync;
var path = require('path');
var fs = require('fs');

console.log('Installing npm dependencies');
execSync('yarn install', {stdio:[0,1,2]});

console.log('Installing sdk dependencies');
var sdkDependency = 'SalesforceMobileSDK-iOS';
var repoUrlWithBranch = packageJson.sdkDependencies[sdkDependency];
var parts = repoUrlWithBranch.split('#'), repoUrl = parts[0], branch = parts.length > 1 ? parts[1] : 'master';
var targetDir = path.join('mobile_sdk', sdkDependency);
if (fs.existsSync(targetDir)) {
    console.log(targetDir + ' already exists - if you want to refresh it, please remove it and re-run install.js');
} else {
    execSync('git clone --branch ' + branch + ' --single-branch --depth 1 ' + repoUrl + ' ' + targetDir, {stdio:[0,1,2]});
}

console.log('Adding .xcode.env');
// Use process.execPath which is the most reliable way to get Node.js path
// This works even with nvm, fnm, or other Node version managers
let nodePath = process.execPath;
// Fallback to which/command -v if process.execPath doesn't work
if (!nodePath || !fs.existsSync(nodePath)) {
    try {
        nodePath = execSync('which node', { encoding: 'utf-8' }).trim();
    } catch (e) {
        try {
            nodePath = execSync('command -v node', { encoding: 'utf-8' }).trim();
        } catch (e2) {
            console.error('Could not find Node.js path. Please set NODE_BINARY manually in ios/.xcode.env');
            process.exit(1);
        }
    }
}
if (!fs.existsSync(nodePath)) {
    console.error(`Node.js path ${nodePath} does not exist. Please set NODE_BINARY manually in ios/.xcode.env`);
    process.exit(1);
}
console.log(`Setting NODE_BINARY to ${nodePath}`);
execSync(`echo export NODE_BINARY=${nodePath} > .xcode.env`, {stdio:[0,1,2], cwd:'ios'});

console.log('Installing pod dependencies');
execSync('pod update', {stdio:[0,1,2], cwd:'ios'});
