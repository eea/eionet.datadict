package eionet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */

@Configuration
@ImportResource("classpath:mock-spring-context.xml")
public class ApplicationTestContext {    
}
