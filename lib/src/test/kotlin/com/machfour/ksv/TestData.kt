package com.machfour.ksv

internal val csvTest1: String = """
mrBranche;mrBeschrTechn;mrStartJahr
Automotive;Kanban, Jira, Confluence;2017
"Information & Kommunikation; Telekommunikation";Netcracker TOMS, Tibco ESB, Schnittstellen zu externen Service-Anbietern (Deutsche Telekom WITA OSS, WITA ESS, WBCI, Arvato, Telefónica SPAIN, Komsa), Allgeier scanview, mobileX FSM, Canon Compart DocBridge, Kundenspezifische Software (Adressmanagement, Verfügbarkeitsprüfung, Verwaltung technischer Ressourcen),  Web Services (SOAP & JMS, XML, WSDL, XSD), SoapUI, Microsoft Office, BOC ADOIT, SparxSystems Enterprise Architect, Atlassian Jira & Confluence;2016
"Information & Kommunikation; Informationstechnologie";AGIS prima, Microsoft SQL Server, Microsoft Reporting Services, SQL Server Report Generator, Microsoft SQL Server Integration Services, Microsoft Visual Studio, Microsoft SQL Server Data Tools, Microsoft .NET, Java Spring Framework, Angular 8, Azure DevOps, Microsoft Office;2018
"Finanzen; Banken";RedHat Enterprise Linux 5 bis 7, Oracle Database 12 bis 19, Apache Tomcat 8 und 9, Apache Webserver, JBOSS 5 bis 7, OpenJDK 8 und 11, Atlassian Confluence, ServiceNow, Icinga, Microsoft Office, verschiedenste Banksysteme im B2B Bereich;2020
Güterverkehr;Microsoft Excel / PowerPoint / Word / Project, Lotus Notes, HPQC, Kunden interne Tools, ClearCase, ClearQuest;2016
Handel;HP-QC 11, MDNG (Masterdata Next Generation), ESB (Enterprise Service Bus) Tracking Tool, M.A.S.H. (Message Administration Service Hospital), UE Warenwirtschaftssystem, S.A.L.D.O. 3 (Sortiment. Altware. Lager. Disposition. Organisation.) PPM (Project & Portfolio Management System), SQL Developer, Jira, Confluence;2016
Telekommunikation;Java, Spring, JDBC/Hibernate, Oracle, Jax-WS, Apache CXF, SOAP/XML, SoapUi;2015
Versandhandel & Broadcast;Microsoft Windows 7/8.1/10, iOS, Android, Citrix XenDesktop, Citrix XenMobile, Citrix ShareFile, Symantec Endpoint Management (Altiris), Symantec Enterprise Vault for Exchange, Microsoft Project 2010, Microsoft Visio 2010, ITIL V3, Cisco Wireless Solution, Ricoh Multifunction System;2015
Telekommunikation;"Scrum
intelliJ
Protractor
GIT
Jira
Large/ small screen devices";2015
Telekommunikation;"Scrum
intelliJ
Protractor
GIT
Jira
Large/ small screen devices";2015            
"""

internal val csvTest1Comma: String = """
mrBranche,mrBeschrTechn,mrStartJahr
Automotive,"Kanban, Jira, Confluence",2017
"Information & Kommunikation, Telekommunikation","Netcracker TOMS, Tibco ESB, Schnittstellen zu externen Service-Anbietern (Deutsche Telekom WITA OSS, WITA ESS, WBCI, Arvato, Telefónica SPAIN, Komsa), Allgeier scanview, mobileX FSM, Canon Compart DocBridge, Kundenspezifische Software (Adressmanagement, Verfügbarkeitsprüfung, Verwaltung technischer Ressourcen),  Web Services (SOAP & JMS, XML, WSDL, XSD), SoapUI, Microsoft Office, BOC ADOIT, SparxSystems Enterprise Architect, Atlassian Jira & Confluence",2016
"Information & Kommunikation, Informationstechnologie","AGIS prima, Microsoft SQL Server, Microsoft Reporting Services, SQL Server Report Generator, Microsoft SQL Server Integration Services, Microsoft Visual Studio, Microsoft SQL Server Data Tools, Microsoft .NET, Java Spring Framework, Angular 8, Azure DevOps, Microsoft Office",2018
"Finanzen, Banken","RedHat Enterprise Linux 5 bis 7, Oracle Database 12 bis 19, Apache Tomcat 8 und 9, Apache Webserver, JBOSS 5 bis 7, OpenJDK 8 und 11, Atlassian Confluence, ServiceNow, Icinga, Microsoft Office, verschiedenste Banksysteme im B2B Bereich",2020
Güterverkehr,"Microsoft Excel / PowerPoint / Word / Project, Lotus Notes, HPQC, Kunden interne Tools, ClearCase, ClearQuest",2016
Handel,"HP-QC 11, MDNG (Masterdata Next Generation), ESB (Enterprise Service Bus) Tracking Tool, M.A.S.H. (Message Administration Service Hospital), UE Warenwirtschaftssystem, S.A.L.D.O. 3 (Sortiment. Altware. Lager. Disposition. Organisation.) PPM (Project & Portfolio Management System), SQL Developer, Jira, Confluence",2016
Telekommunikation,"Java, Spring, JDBC/Hibernate, Oracle, Jax-WS, Apache CXF, SOAP/XML, SoapUi",2015
Versandhandel & Broadcast,"Microsoft Windows 7/8.1/10, iOS, Android, Citrix XenDesktop, Citrix XenMobile, Citrix ShareFile, Symantec Endpoint Management (Altiris), Symantec Enterprise Vault for Exchange, Microsoft Project 2010, Microsoft Visio 2010, ITIL V3, Cisco Wireless Solution, Ricoh Multifunction System",2015
Telekommunikation,"Scrum
intelliJ
Protractor
GIT
Jira
Large/ small screen devices",2015
Telekommunikation,"Scrum
intelliJ
Protractor
GIT
Jira
Large/ small screen devices",2015            
"""

internal val csvTest2 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices";2015            """.trimIndent()

internal val csvTest3 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices";2015            
        """.trimIndent()

internal val csvTest4 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices;2015            
        """.trimIndent()

internal val csvTest5 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices";2015            \""".trimIndent()

internal val csvTest6 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices"2015""".trimIndent()

internal val csvTest7 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT\"
        Jira""
        Large/ small screen devices";2015""".trimIndent()

internal val csvTest8 = """
        Telekommunikation;Scrum\
        intelliJ\
        Protractor\
        GIT\" ""\
        Jira\
        Large/ small screen devices\;;2015""".trimIndent()

internal val csvTest9 = """
    1
    2
    3
    4
    5
    6
    7""".trimIndent()

internal val csvTest10 = "1\r\n\"2\r\n3\""
