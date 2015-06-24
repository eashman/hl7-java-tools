As a long time HL7 user, I've developed some preferred methodologies for handling HL7 messages. As a relatively new hand to java, this project seems like a good way to make these tools available, and provide some freely available functionality, with a preference for simplicity and practical applicability in a working clinical environment.


# Javadocs #

may be found at http://unconxio.us/HL7/Java/us.conxio.hl7/

# Applications #

There are two applications included in this project so far. Executables are avaialble in the download section. Source for them is available in the repository. If these facilities enable the creation of any other useful applications, please feel add them.

## hl7MessageService ##

...is a HL7 message service point. It is essentially a thread pooled server which accepts HL7 messages for optional transformation and subsequent routing to one or more destinations. A simple HL7 message log service is provided as an example. See the javadocs   for configuration information.

## hl7MessageAgent ##

...is a simple point to point message transfer agent which can be used to remotely submit transactions to the hl7MessageService, or any MLLP compliant HL7 server.

## Yet to be done ##

Secure i/o.

## Contact ##

Should you have any questions or comments please feel free to contact us at <a href='mailto:hl7@unconxio.us'>hl7@unconxio.us</a>