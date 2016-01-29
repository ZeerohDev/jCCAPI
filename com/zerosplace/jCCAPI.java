package com.zerosplace;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class jCCAPI {

	private String ip;
    private String currentProc;

    public jCCAPI(){}

    private String getIP()
    {
        return this.ip;
    }
    
    private void setIP(String ip)
    {
    	this.ip = ip;
    }

    private String getProc()
    {
        return this.currentProc;
    }

    private void setCurrentProc(String pid)
    {
        this.currentProc = pid;
    }
    
    private String strToHex(String string)
    {
        char[] chars = string.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++)
        {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }
    
    private String hexToStr(String hex)
    {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hex.length(); i += 2)
        {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }
    
    public boolean writeString(int address, String string)
    {	
        String addr = Integer.toHexString(address);
        String bytes = strToHex(string);
        
        String res = handleRequest("setmemory", false, "pid=" + getProc(), "addr=" + addr, "value=" + bytes);
    	switch (res)
    	{
    		case "0":
    			return true;
    		default: 
    			return false;
    	}
    }
    
    public String readString(int address, int size)
    {
    	String addr = Integer.toHexString(address);
    	String resp = handleRequest("getmemory", false, "pid=" + getProc(), "addr=" + addr, "size=" + Integer.toString(size));
    	String string = resp.substring(1);
    	return hexToStr(string);
    }

    public int[] getMemory(int address, int size) //RIP to no int or byte arrays from string with hex :(
    {
    	String addr = Integer.toHexString(address);
    	String resp = handleRequest("getmemory", false, "pid=" + getProc(), "addr=" + addr, "size=" + size);
        int[] bytes = new int[size];
        int arrIndex = 0;
        int startIndex = 1;
        int times = 1;
        while (times != size + 1)
        {
        	bytes[arrIndex] = Integer.parseInt(resp.substring(startIndex, startIndex + 2), 16);
        	arrIndex++;
            startIndex += 2;
            times++;
        }
        return bytes;
    }

    public boolean setMemory(int address, int[] value)
    {
        StringBuilder byteBuilder = new StringBuilder();
        String addr = Integer.toHexString(address);
        for(int b : value)
        {
            if (b < 10)
            {
                byteBuilder.append("0" + Integer.toHexString(b));
            }
            else
            {
                byteBuilder.append(Integer.toHexString(b));
            }
        }
        String res = handleRequest("setmemory", false, "pid=" + getProc(), "addr=" + addr, "value=" + byteBuilder.toString());
        switch (res)
        {
            case "0":
                return true;
            default:
                return false;
        }
    }

    private String[] listProcesses()
    {   
    	int arrIndex = 0;
        String[] subs = new String[3];
        int startIndex = 1;
        String resp = handleRequest("getprocesslist", false, null);
        int times = 0;
        while (times != 3)
        {
        	subs[arrIndex] = resp.substring(startIndex, startIndex + 7);
        	arrIndex++;
        	startIndex += 7;
        	times++;
        }
        return subs;
    }

    private String getProcessName(String pid)
    {
       return handleRequest("getprocessname", false, "pid=" + pid);
    }
    
    public boolean connect(String ip)
    {
    	setIP(ip);
    	return true;
    }

    public boolean attach()
    {
    	String[] procs = null;
    	try
    	{
            procs = listProcesses();
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
        for (String s : procs)
        {
            if (getProcessName(s).contains("EBOOT.BIN"))
            {
                setCurrentProc(s);
                return true;
            }
        }
        return false;
    }

    public boolean notify(String notifyIcon, String msg)
    {
    	String ret = handleRequest("notify", false, "id=" + notifyIcon, "msg=" + msg);
        switch (ret)
        {
            case "0":
                return true;
            default:
                return false;
        }
    }
    
    public boolean ledColor(int color, int status)
    {
    	String ret = handleRequest("setconsoleled", false, "color=" + Integer.toString(color), "status=" + status);
        switch (ret)
        {
            case "0":
                return true;
            default:
                return false;
        }
    }
    
    public boolean ringBuzzer(int type)
    {
    	String ret = handleRequest("ringbuzzer", false, "type=" + Integer.toString(type));
        switch (ret)
        {
            case "0":
                return true;
            default:
                return false;
        }
    }
    
    public int getCellTempCelsius()
    {
    	int arrIndex = 0;
        String[] subs = new String[2];
        int startIndex = 1;
        String resp = handleRequest("gettemperature", false, null);
        int times = 0;
        while (times != 2)
        {
        	subs[arrIndex] = resp.substring(startIndex, startIndex + 2);
        	arrIndex++;
        	startIndex += 2;
        	times++;
        }
    	return Integer.parseInt(subs[0], 16);
    }
    
    public int getCellTempFahrenheit()
    {
    	return getCellTempCelsius() * 9 / 5 + 32;
    }
    
    public int getRsxTempCelsius()
    {
    	int arrIndex = 0;
        String[] subs = new String[2];
        int startIndex = 1;
        String resp = handleRequest("gettemperature", false, null);
        int times = 0;
        while (times != 2)
        {
        	subs[arrIndex] = resp.substring(startIndex, startIndex + 2);
        	arrIndex++;
        	startIndex += 2;
        	times++;
        }
    	return Integer.parseInt(subs[1], 16);
    }
    
    public int getRsxTempFahrenheit()
    {
    	return getRsxTempCelsius() * 9 / 5 + 32;
    }

    public void shutDown(int mode)
    {
        handleRequest("shutdown", true, Integer.toString(mode));
    }
    
    public boolean setBootConsoleID(String cid)
    {
    	String resp = handleRequest("setbootconsoleids", false, "type=" + IdTypes.Idps, "on=1", "id=" + cid);
    	switch (resp)
    	{
    	case "0":
    		return true;
    	default:
    		return false;
    	}
    }
    
    public boolean resetBootConsoleID()
    {
    	String resp = handleRequest("setbootconsoleids", false, "type=" + IdTypes.Idps, "on=0", "id=NULL");
    	switch (resp)
    	{
    	case "0":
    		return true;
    	default:
    		return false;
    	}
    }
    
    public boolean setBootPsid(String psid)
    {
    	String resp = handleRequest("setbootconsoleids", false, "type=" + IdTypes.Psid, "on=1", "id=" + psid);
    	switch (resp)
    	{
    	case "0":
    		return true;
    	default:
    		return false;
    	}
    }
    
    public boolean resetBootPsid()
    {
    	String resp = handleRequest("setbootconsoleids", false, "type=" + IdTypes.Psid, "on=0", "id=NULL");
    	switch (resp)
    	{
    	case "0":
    		return true;
    	default:
    		return false;
    	}
    }
    
    private String handleRequest(String command, boolean isVoid, String...args)
    {
    	try
    	{
    		URL url = null;
        	if (args != null)
        	{
        		StringBuilder params = new StringBuilder();
        		int paramCount = 0;
        		for (String s : args)
        		{
        			if (args.length > 1)
        			{
        				if (paramCount == args.length - args.length)
        				{
        					params.append(s);
        				}
        				else
        				{
        					params.append("&" + s);
        				}
        				paramCount++;
        			}
        			else
        			{
        				params.append(s);
        			}
        		}
        		System.out.println("http://" + getIP() + ":6333/ccapi/" + command + "?" + params.toString());
        		url = new URL("http://" + getIP() + ":6333/ccapi/" + command + "?" + params.toString());
        	}
        	else
        	{
        		url = new URL("http://" + getIP() + ":6333/ccapi/" + command);
        	}
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
    		if (isVoid)
    		{
    			return null;
    		}
    		else
    		{
    			BufferedReader in = new BufferedReader( new InputStreamReader(client.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                	response.append(inputLine);
                }
        		in.close();
        		
        		return response.toString();
    		}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    }
}
