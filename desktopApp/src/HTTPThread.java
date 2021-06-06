
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Johannes Loibner
 */
public class HTTPThread extends Thread
{

    private final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<>();
    private UserData user;
    private String server;
    private String message;
    private HttpsRequestPoster hrp;
    
    public HTTPThread(UserData user, String server)
    {
        this.user = user;
        this.server = server;
        hrp = new HttpsRequestPoster();
    }
    
    public final void addListener(final ThreadCompleteListener listener)
    {
        listeners.add(listener);
    }
    
    public final void removeListener(final ThreadCompleteListener listener)
    {
        listeners.remove(listener);
    }
    
    private final void notifyListeners()
    {
        for(ThreadCompleteListener listener : listeners)
        {
            listener.notifyOfThreadComplete(message);
        }
    }
    
    private int waitForMessageOnServer()
    {
        String url = server + "/Data/GetMsgPost";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userPhone", user.getPhoneNumber());
        parameters.put("userId", user.getMsgId()+"");
        String text = hrp.makePostRequest(url, parameters);
        if(text.split(";")[1].endsWith("200"))
        {
            message = text.split(";")[0];
            return 1;
        }
        else
        {
            return 0;
        }
    }
    
    public void doRun()
    {
        while(waitForMessageOnServer() != 1)
        {

        }
        System.out.println("Message: " + message);
    }

    @Override
    public void run() 
    {
        try
        {
            doRun();
        }
        finally
        {
            notifyListeners();
        }
    }
    
}
