![alt tag](images/rbc_logo.png)

-----

# openmrs-module-rbcmessaging
Messaging Module for Rwanda Biomedical Center

## Sending SMS from another OpenMRS module

Add the API artifact to the consuming module:

```xml
<dependency>
    <groupId>org.openmrs.module</groupId>
    <artifactId>rbcmessaging-api</artifactId>
    <version>1.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Declare `org.openmrs.module.rbcmessaging` as a required module in the consuming module's
`config.xml`. Then obtain the service through the OpenMRS context:

```java
RBCMessagingService messagingService = Context.getService(RBCMessagingService.class);

// Uses the person attribute configured by rbcmessaging.phoneAttribute.
SmsResult result = messagingService.sendSms(patient, "Your appointment is tomorrow");

// Or send to a number already resolved by the caller.
SmsResult result = messagingService.sendSms("250788123456", "Your appointment is tomorrow");
```

The service routes each request through the vendor selected on the SMS configuration page. A
successful result contains the vendor name and its message ID. Invalid input and vendor failures
are reported as `APIException`.
