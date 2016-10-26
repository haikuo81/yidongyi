package com.sectong.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sectong.domain.Sms;
import com.sectong.repository.SmsRepository;
import com.sectong.thirdparty.sms.SendSMS;

/**
 * 短信服务
 * 
 * @author jiekechoo
 *
 */
@Service
public class SendSMSServiceImpl implements SendSMSService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendSMSServiceImpl.class);
	private SmsRepository smsRepository;

	@Autowired
	public SendSMSServiceImpl(SmsRepository smsRepository) {
		this.smsRepository = smsRepository;
	}

	/**
	 * 发送短信
	 */
	@Override
	public String send(String mobile) {
		String url = "http://sms.253.com/msg/";// 应用地址
		String account = "N8528646";// 账号
		String pswd = "aIpOKDt2bu908a";// 密码
		int mobile_code = (int) ((Math.random() * 9 + 1) * 100000); // 验证码
		String msg = "【253云通讯】您好，您的验证码是" + mobile_code;// 短信内容
		Boolean needstatus = true;// 是否需要状态报告，需要true，不需要false
		String extno = null;// 扩展码

		try {
			String returnString = SendSMS.batchSend(url, account, pswd, mobile, msg, needstatus, extno);
			LOGGER.info(returnString);
			// 20161026182305,0\n
			// 16102618230521663
			String code = returnString.split("\n")[0].split(",")[1];

			// 保存短信验证码
			Sms sms = new Sms();
			Date today = new Date();
			Date expireDate = new Date(today.getTime() + (1000 * 60 * 30)); // 短信半小时后过期
			sms.createSms(mobile, String.valueOf(mobile_code), expireDate);
			smsRepository.save(sms);

			return code;
			// TODO 处理返回值,参见HTTP协议文档
		} catch (Exception e) {
			// TODO 处理异常
			e.printStackTrace();
			return "error";
		}
	}

	@Override
	public Sms findByUsernameAndVcode(String mobile, String vcode) {
		Sms sms = new Sms();
		sms = smsRepository.findFirstByMobileAndVcodeOrderByExpiredDatetimeDesc(mobile, vcode);
		LOGGER.info("Finding username and vcode");
		if (sms != null && (sms.getExpiredDatetime().getTime() > new Date().getTime())) {
			LOGGER.info("username/vcode matched and valid");
			return sms;
		}
		LOGGER.info("username/vcode not matched and invalid");
		return null;
	}

}
