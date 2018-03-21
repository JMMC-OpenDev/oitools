#!/bin/bash
#
# Git commands to update your forked repository with the JMMC-OpenDev/OITools master
#

# First, configure the remote upstream:
# git remote add upstream https://github.com/ORIGINAL_OWNER/ORIGINAL_REPOSITORY.git

# Get remote changes into the local branch 'upstream'
git fetch -v upstream 

# Set local branch to 'master' (useless but to be sure)
git checkout master 

# Merge the local branch 'upstream' into the local branch to 'master'
# May create conflicts with any local change (need to push next)
git merge -v upstream/master

echo "------------------------------------------------------"
echo " 1. Check your local git repository:"
echo "# git status"
echo " 2. Push your local git merge into remote fork"
echo "# git push"
echo "------------------------------------------------------"

echo "Local changes:"
git status
