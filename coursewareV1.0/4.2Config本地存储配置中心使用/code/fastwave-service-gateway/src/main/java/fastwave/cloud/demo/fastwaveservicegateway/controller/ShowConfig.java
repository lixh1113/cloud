package fastwave.cloud.demo.fastwaveservicegateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShowConfig {

    @Value("${custom.pic.path}")
    String path;

    @GetMapping("/showConfig")
    public String showConfig()
    {
        return "获取到文件存放的路径是：" + path;
    }
}
