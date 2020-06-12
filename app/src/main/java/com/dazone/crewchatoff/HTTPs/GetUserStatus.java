package com.dazone.crewchatoff.HTTPs;

import android.util.Log;

import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.StatusDto;
import com.dazone.crewchatoff.dto.StatusItemDto;
import com.dazone.crewchatoff.utils.Prefs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class GetUserStatus {
	public StatusDto getStatusOfUsers(String domain, int companyNo) {
		StatusDto status = new StatusDto();

		try {
			String command = "idstatus";
			String id = companyNo + "_*";

			InetAddress serverAddr = InetAddress.getByName(domain);
			Socket socket = new Socket(serverAddr, new Prefs().getDDS_SERVER_PORT());
			InputStream input = socket.getInputStream();
			Reader reader = new InputStreamReader(input);
			BufferedReader bufferedReader = new BufferedReader(reader);

			OutputStream output = socket.getOutputStream();
			Writer writer = new OutputStreamWriter(output);

			writer.write(String.format("#%d#%s=%s", command.length() + 1 + id.length(), command, id));
			writer.flush();

			ArrayList<StatusItemDto> mapUsers = new ArrayList<>();

			String list = bufferedReader.readLine();

			writer.close();
			output.close();

			bufferedReader.close();
			reader.close();
			input.close();

			socket.close();
			list = list.substring(list.indexOf(",") + 1);
			String[] users = list.split("/");
			for (String user : users) {
				String[] infos = user.split(",");
				String userId = infos[0].replace(id.replace("*", ""), "");
				int time = -1;

				try {
					time = Integer.valueOf(infos[2]);
					StatusItemDto item = new StatusItemDto();
					item.setUserID(userId);
					if (time >= Statics.USER_STATUS_AWAY_TIME) {
						item.setStatus(3);
					} else {
						item.setStatus(1);
					}
					mapUsers.add(item);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			status.setItems(mapUsers);

			return status;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}