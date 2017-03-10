package eionet.datadict.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Controller
public class DataSetController {
    
    @RequestMapping(value="/testmvc", method = RequestMethod.GET)
    @ResponseBody
    public String testMVCINDD(){
      return "it works";
    }
}
