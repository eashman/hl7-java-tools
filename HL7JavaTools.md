# Introduction #

As a long time HL7 user, I've developed some preferred methodologies for handling HL7 messages. As a relatively new hand to java, this project seems like a good way to make these tools available, and provide some freely available functionality, with a preference for simplicity and practical applicability in a working clinical environment.


# Javadocs #

may be found at http://unconxio.us/HL7/Java/us.conxio.hl7/

# Applications #

There are two applications included in this project so far. If these facilities enable the creation of any other useful applications, please feel add them.

## HL7MessageServiceRunner ##

...is a HL7 message service point. It is essentially a thread pooled server which accepts HL7 messages for subsequent optional transformation and routing to one or more destinations. A simple HL7 message log service is planned as an example. It should be appearing in the downloads section soon. See the javadocs for the HL7MessageService class for configuration information.

## HL7MessageAgent ##

...is a simple point to point message transfer agent which can be used to submit transactions to the HL7MessageServiceRunner.

## Yet to be done ##

Secure i/o.