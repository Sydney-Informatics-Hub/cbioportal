# Ansible Role: cBioPortal

Updates cBioPortal for RedHat/CentOS 7.x servers, after previous install by Intersect

## Requirements

    - "devel" user account on target machine
    - the hostname on the target machine needs to be set to the fully qualified domain name

## Role Variables

Available variables are listed below, along with default values:

    cbioportal_git_tag: HEAD

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

    internal_path_report_root

Root directory of where the internal pathology reports are stored, for example "/home/devel/data/pathology_reports"

    internal_ca125_plot_root

Root directory of where the internal CA125 plots are stored, for example "/home/devel/data/ca125_plots"

    internal_molecular_testing_report_root

Root directory of where the internal molecular testing reports are stored, for example "/home/devel/data/molecular_testing_reports"

    internal_slide_images_root

Root directory of where the internal slide images are stored, for example "/home/devel/data/slide_images"

## Role Tags

To skip a specific role tag include the argument "--skip-tags <tag>" when running the playbook.

    clean

Tasks related to performing a clean deployment of cBioPortal. These tasks can be skipping when redeploying cBioPortal.

## Dependencies
  - vips
