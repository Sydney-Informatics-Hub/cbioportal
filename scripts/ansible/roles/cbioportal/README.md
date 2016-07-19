# Ansible Role: cBioPortal

Installs cBioPortal for RedHat/CentOS 7.x servers.

## Requirements

    - "devel" user account on target machine

## Role Variables

Available variables are listed below, along with default values:

    cbioportal_git_tag: HEAD

Set the branch/tag of the forked cBioPortal repository to use. See https://github.com/IntersectAustralia/cbioportal for available branches/tags.

## Role Tags

To skip a specific role tag include the argument "--skip-tags <tag>" when running the playbook.


    loadSampleStudy

Tasks related to loading the sample study used to verify that cBioPortal is functioning correctly.

    clean

Tasks related to performing a clean deployment of cBioPortal. These tasks can be skipping when redeploying cBioPortal.

## Dependencies
  - selinux
  - iptables
  - mysql
  - tomcat7
  - java
  - maven
  - apache
