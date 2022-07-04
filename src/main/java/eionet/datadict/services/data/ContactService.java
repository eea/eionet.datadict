package eionet.datadict.services.data;

import eionet.datadict.model.ContactDetails;

import java.util.List;
import java.util.Set;

public interface ContactService {

    /**
     * fetches all records for the specific contact
     * @param value
     * @return
     */
     Set<ContactDetails> getAllByValue(String value);
}
