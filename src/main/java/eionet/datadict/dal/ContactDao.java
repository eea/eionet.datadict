package eionet.datadict.dal;

import eionet.datadict.model.ContactDetails;

import java.util.Set;

public interface ContactDao {

    /**
     * fetches all records for the specific contact
     * @param value
     * @return
     */
    public Set<ContactDetails> getAllByValue(String value);
}
