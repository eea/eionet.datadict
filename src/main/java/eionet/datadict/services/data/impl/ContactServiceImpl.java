package eionet.datadict.services.data.impl;

import eionet.datadict.dal.ContactDao;
import eionet.datadict.model.ContactDetails;
import eionet.datadict.services.data.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    private ContactDao contactDao;

    @Autowired
    public ContactServiceImpl(ContactDao contactDao) {
        this.contactDao = contactDao;
    }

    @Override
    public List<ContactDetails> getAllByValue(String value) {
        return contactDao.getAllByValue(value);
    }
}
