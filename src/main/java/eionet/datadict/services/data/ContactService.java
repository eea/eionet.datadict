package eionet.datadict.services.data;

import eionet.datadict.model.ContactDetails;

import java.util.List;

public interface ContactService {

    /**
     * fetches all records for the specific contact
     * @param value
     * @return
     */
    public List<ContactDetails> getAllByValue(String value);
}
