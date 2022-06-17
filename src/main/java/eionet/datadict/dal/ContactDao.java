package eionet.datadict.dal;

import eionet.datadict.model.ContactDetails;

import java.util.List;

public interface ContactDao {

    /**
     * fetches all records for the specific contact
     * @param value
     * @return
     */
    public List<ContactDetails> getAllByValue(String value);
}
