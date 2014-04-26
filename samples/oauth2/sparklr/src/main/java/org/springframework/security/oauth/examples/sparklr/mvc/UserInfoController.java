package org.springframework.security.oauth.examples.sparklr.mvc;

import java.security.Principal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserInfoController {
  
  @RequestMapping(value = "/userinfo")
  public ResponseEntity<String> getUserInfo(@RequestParam(value = "openid", required = false) String openid, Principal principal) {
    StringBuilder out = new StringBuilder();
    out.append("{");
    out.append(String.format("\"openid\":\"%s\",", openid));
    out.append("\"nickname\": \"\",");
    out.append("\"sex\":\"1\",");
    out.append("\"province\":\"PROVINCE\",");
    out.append("\"city\":\"CITY\",");
    out.append("\"country\":\"COUNTRY\",");
    out.append("\"headimgurl\":    \"http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46\","); 
    out.append("\"privilege\":[");
    out.append("\"PRIVILEGE1\",");
    out.append("\"PRIVILEGE2\"");
    out.append("]");
    out.append("}");
    
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    return new ResponseEntity<String>(out.toString(), headers, HttpStatus.OK);
  }
  
}
