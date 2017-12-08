package cn.skyeye.norths.actions;

import cn.skyeye.norths.NorthContext;
import cn.skyeye.norths.utils.ResponseHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/8 10:24
 */
@RestController
@RequestMapping("/norths/config/")
public class UploadController {

    protected final Log logger = LogFactory.getLog(UploadController.class);

    private NorthContext northContext = NorthContext.get();

    @ResponseBody
    @RequestMapping(value = "mailreceiver/upload", method = RequestMethod.POST)
    public Object uploadFile(@RequestParam("receiverfile") MultipartFile receiverfile) {

        InputStream inputStream = null;
        String originalFilename = receiverfile.getOriginalFilename();

        try {
            inputStream = receiverfile.getInputStream();
            List<String> strings = IOUtils.readLines(inputStream);



            System.err.println("---" + strings + "---");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
        System.err.println(inputStream);

        return ResponseHelper.success();
    }
}
