/*
 * Copyright (c) 2020 CertifAI
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.classifai.ui;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.JOptionPane.showMessageDialog;

/***
 * Handler to open classifai in browser
 *
 * @author Chiawei Lim
 */
@Slf4j
public class ChromiumHandler {

    private final static String PRIMARY_KEY = "chromium";
    private final static String SECONDARY_KEY = "chrome";
    private final static String ICON_KEY = "icon";

    private static Map<String, String> macBrowserKey;
    private static Map<String, String> winBrowserKey;

    static
    {
        macBrowserKey = new HashMap<>();

        //FIXME: Is there a way to fix this coded path?
        macBrowserKey.put(PRIMARY_KEY, "/Applications/classifai.app/Contents/app/chrome-mac/Chromium.app");
        macBrowserKey.put(SECONDARY_KEY, "/Applications/Google Chrome.app");

        winBrowserKey = new HashMap<>();

        String chromeNativePath = System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";

        winBrowserKey.put(PRIMARY_KEY, "app\\chrome-win\\chrome.exe");
        winBrowserKey.put(SECONDARY_KEY, chromeNativePath);

        InputStream iconStream = ChromiumHandler.class.getClassLoader().getResourceAsStream("icon/Classifai_Favicon_Dark_32px.png");

        try {

            File iconPath = new File(System.getProperty("java.io.tmpdir") + File.separator +  "iconBuffer.png");

            FileUtils.copyInputStreamToFile(iconStream, iconPath);

            macBrowserKey.put(ICON_KEY, iconPath.getAbsolutePath());
            winBrowserKey.put(ICON_KEY, iconPath.getAbsolutePath());
        }
        catch (Exception e)
        {
            log.info("Icon not found, ", e);
        }
    }

    public static void openOnBrowser(String url, OSManager osManager)
    {
        String[] commandPath = null;
        String browserNotFoundMessage = "Browser not found.\nProceed to open " + url + " in other browser";
        String osNotSupportedMessage = "OS not supported.\nProceed to open " + url + " in other browser";

        OS currentOS = osManager.getCurrentOS();

        //https://stackoverflow.com/questions/45660482/open-a-url-in-chrome-using-java-in-linux-and-mac/45660804
        //chromium primary, chrome secondary, else failure
        if(currentOS.equals(OS.MAC))
        {
            String param1 = "/usr/bin/open";
            String param2 = "-a";

            if(isBrowserFileExist(macBrowserKey.get(PRIMARY_KEY)))
            {
                commandPath = new String[]{param1, param2, macBrowserKey.get(PRIMARY_KEY), url};
            }
            else if(isBrowserFileExist(macBrowserKey.get(SECONDARY_KEY)))
            {
                commandPath = new String[]{param1, param2, macBrowserKey.get(SECONDARY_KEY), url};
            }
            else
            {

                failToOpenBrowserMessage(url, macBrowserKey.get(ICON_KEY), browserNotFoundMessage);
            }
        }
        else if(currentOS.equals(OS.WINDOWS))
        {
            String param1 = "cmd";
            String param2 = "/c";

            if(isBrowserFileExist(winBrowserKey.get(PRIMARY_KEY)))
            {
                commandPath = new String[]{param1, param2, "start " + winBrowserKey.get(PRIMARY_KEY) + " " + url};
            }
            else if(isBrowserFileExist(winBrowserKey.get(SECONDARY_KEY)))
            {
                commandPath = new String[]{param1, param2, "start " + winBrowserKey.get(SECONDARY_KEY) + " " + url};
            }
            else
            {
                failToOpenBrowserMessage(url, winBrowserKey.get(ICON_KEY), browserNotFoundMessage);
            }
        }
        else
        {
            log.debug("Browser in " + currentOS.toString() + " not supported yet");
            failToOpenBrowserMessage(url, winBrowserKey.get(ICON_KEY), osNotSupportedMessage);
            return;
        }

        if(commandPath != null)
        {
            try
            {
                Runtime.getRuntime().exec(commandPath);
            }
            catch(Exception e)
            {
                log.debug(currentOS.toString() + " - Failed to open classifai. ", e);
            }
        }

    }

    public static boolean isBrowserFileExist(String appPath)
    {
        if(new File(appPath).exists() == false)
        {
            log.debug("Chromium browser not found.");

            return false;
        }

        return true;
    }

    public static void failToOpenBrowserMessage(String url, String iconPath, String message)
    {
        ImageIcon messageIcon = null;

        if(iconPath != null)
        {
            messageIcon = new ImageIcon(iconPath);
        }

        showMessageDialog(null, message,
                "Oops!", JOptionPane.INFORMATION_MESSAGE, messageIcon);
    }
}