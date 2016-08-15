# Ansible Role: cBioPortal

Installs cBioPortal for RedHat/CentOS 7.x servers.

## Requirements

    - "devel" user account on target machine
    - a valid SSL .crt file on the target machine in the location specified in the SSLCertificateFile variable
    - a valid SSL .key file on the target machine in the location specified in the SSLCertificateKeyFile variable
    - a valid SSL .ca-bundle file on the target machine in the location specified in the SSLCACertificateFile variable
    - the hostname on the target machine needs to be set to the fully qualified domain name

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

    load_sample_study: false

Validates and loads the sample study provided with cBioPortal.

    service_login_initiation_url
    
Unique URL for the application to inititation login with Rapid Connect, provided once service registration is approved

    service_primary_url

Primary URL of the application provided as part of service registration, e.g. https://westmead-cbioportal.intersect.org.au/cbioportal

    service_issuer
    
Rapid connect environment used to issue the service, "https://rapid.aaf.edu.au" when in the production environment or "https://rapid.test.aaf.edu.au" when in the test environment

    service_shared_secret
    
Secret value shared between the service and Rapid Connect, as part of registration, for token encryption and verification. 
Note: any backslash (\) characters within the shared-secret MUST be escaped with an additional backslash (\\), otherwise the backslash will be silently dropped when the property is loaded within cBioPortal.

## Role Tags

To skip a specific role tag include the argument "--skip-tags <tag>" when running the playbook.

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
