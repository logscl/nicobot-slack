package com.st.nicobot.internal.services.memory;

import javax.annotation.PostConstruct;
import java.io.*;

/**
 * Created by Logs on 17-08-15.
 */
public abstract class AbstractRepositoryManager<T> {

    protected abstract T getMemory();

    protected abstract void setMemory(Object memory);

    protected abstract String getMemoryFileName();

    protected abstract void initMemory();

    protected boolean memoryLoaded = false;

    @PostConstruct
    protected boolean loadFile() {
        if (!memoryLoaded) {
            try {
                FileInputStream fin = new FileInputStream(getMemoryFileName());
                ObjectInputStream ois = new ObjectInputStream(fin);
                setMemory(ois.readObject());
                fin.close();
                memoryLoaded = true;
            } catch (FileNotFoundException fe) {
                initMemory();
                memoryLoaded = true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    protected boolean writeFile() {
        try {
            FileOutputStream fout = new FileOutputStream(getMemoryFileName());
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(getMemory());
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
