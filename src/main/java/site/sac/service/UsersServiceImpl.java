package site.sac.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.sac.dto.GoogleOAuthDTO;
import site.sac.dto.JWTPayloadDTO;
import site.sac.dto.TokenDTO;
import site.sac.dto.UsersDTO;
import site.sac.mapper.TokenMapper;
import site.sac.mapper.UsersMapper;

import java.util.Base64;
@Slf4j
@Service
public class UsersServiceImpl implements UsersService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private TokenMapper tokenMapper;

    @Override
    public String register(GoogleOAuthDTO googleOAuthDTO) {
        JWTPayloadDTO payload = getPayload(googleOAuthDTO);
        String token = DigestUtils.sha256Hex(payload.getEmail());
        log.info(token.toString() + " 2번째 접근");
        if (isExistToken(token) == false) {
            registerUser(payload);
            registerToken(token, usersMapper.select((payload.getEmail())));
        }

        return token;
    }

    private void registerUser(JWTPayloadDTO payload) {
        UsersDTO users = new UsersDTO();
        users.setUser_email(payload.getEmail());
        users.setUser_image(payload.getPicture());
        users.setUser_name(payload.getName());

        usersMapper.insert(users);
    }

    private void registerToken(String token, long users_id) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        tokenDTO.setUser_id(users_id);

        tokenMapper.insert(tokenDTO);
    }

    private JWTPayloadDTO getPayload(GoogleOAuthDTO googleOAuth) {
        String[] chunks = googleOAuth.getCredential().split("\\.");
        log.info(chunks.toString());
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payloadString = new String(decoder.decode(chunks[1]));

        log.info("-0--------------");
        log.info(payloadString);
        return stringToJSON(payloadString);
    }

    private JWTPayloadDTO stringToJSON(String jsonString) {
        JWTPayloadDTO payload = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            payload = objectMapper.readValue(jsonString, JWTPayloadDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payload;
    }

    @Override
    public boolean isExistToken(String token) {
        return tokenMapper.select(token) != null;
    }

    @Override
    public long findUserIdByToken(String token) {

        return tokenMapper.select(token).getUser_id();
    }
}
