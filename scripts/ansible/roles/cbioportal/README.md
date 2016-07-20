# Ansible Role: cBioPortal

Installs cBioPortal for RedHat/CentOS 7.x servers.

## Requirements

    - "devel" user account on target machine
    - a valid SSL .crt file on the target machine in the location specified in the SSLCertificateFile variable
    - a valid SSL .key file on the target machine in the location specified in the SSLCertificateKeyFile variable
    - a valid SSL .ca-bundle file on the target machine in the location specified in the SSLCACertificateFile variable

## Role Variables

Available variables are listed below, along with default values:

    cbioportal_git_tag: HEAD

Set the branch/tag of the forked cBioPortal repository to use. See https://github.com/IntersectAustralia/cbioportal for available branches/tags.

    SSLCertificateFile

Path to the SSL .crt certificate file to use with HTTPS.

    SSLCertificateKeyFile

Path to the SSL .key certificate key file to use with HTTPS.

    SSLCACertificateFile

Path to the SSL .ca-bundle certificate bundle file to use with HTTPS.

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
