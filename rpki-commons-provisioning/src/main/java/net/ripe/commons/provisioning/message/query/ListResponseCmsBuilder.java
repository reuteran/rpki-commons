package net.ripe.commons.provisioning.message.query;

import net.ripe.commons.provisioning.message.PayloadMessageType;
import net.ripe.commons.provisioning.message.common.ResourceClassCmsBuilder;

public class ListResponseCmsBuilder extends ResourceClassCmsBuilder {

    public ListResponseCmsBuilder() {
        super(PayloadMessageType.list_response);
    }
}