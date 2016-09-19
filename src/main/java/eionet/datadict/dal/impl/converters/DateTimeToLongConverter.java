package eionet.datadict.dal.impl.converters;

import eionet.datadict.data.DataConverter;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component("dateTimeToLongConverter")
public class DateTimeToLongConverter implements DataConverter {

    @Override
    public Long convert(Object value) {
        return value == null ? null : ((Date) value).getTime();
    }

    @Override
    public Date convertBack(Object value) {
        return value == null ? null : new Date((Long) value);
    }
    
}
