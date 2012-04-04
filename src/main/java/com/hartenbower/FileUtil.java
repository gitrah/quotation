package com.hartenbower;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import scala.Tuple2;
import scala.Tuple3;

public class FileUtil {

    final static Logger log = Logger.getLogger(FileUtil.class);
    final static int BUFF_SIZE = 1024 * 10;

    public FileUtil() {
    }

    public static String fromFile(File f, int position, int size)
            throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
            reader.skip(position);
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null
                    && stringBuilder.length() < size) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            return stringBuilder.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static synchronized void truncateFromBeginning(File f, double pct, File remnant)
            throws IOException {

        RandomAccessFile raf = null;
        RandomAccessFile rafTemp = null;
        File fTemp = null;
        try {
            raf = new RandomAccessFile(f, "rw");
            fTemp = remnant == null ? new File(f.getAbsolutePath() + ".tmp") : remnant;
            rafTemp = fTemp == null ? null : new RandomAccessFile(fTemp, "rw");
            FileChannel fc = raf.getChannel();
            FileChannel fcTemp = rafTemp == null ? fc : rafTemp.getChannel();
            fc.lock();
            long oldSize = f.length();
            long newSize = (long) (oldSize * (1. - pct));
            log.debug("oldSize " + oldSize + ", newSize " + newSize);
            fc.transferTo(oldSize - newSize, newSize, fcTemp); // transfer
                                                               // surviving
                                                               // chunk to temp
                                                               // file
            fcTemp.transferTo(0, newSize, fc); // overwrite beginning of log
                                               // with surviving chunk
            fc.truncate(newSize); // trim remaining excess from log
        }catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } finally {
                try {
                    if (rafTemp != raf && rafTemp != null) {
                        rafTemp.close();
                    }
                } finally {
                    if (remnant == null && fTemp != null && fTemp.exists()) {
                        fTemp.delete();
                    }
                }
            }
        }
    }

    public static String toXml(Object thing) {
        XMLEncoder encoder = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream(BUFF_SIZE);
            encoder = new XMLEncoder(baos);
            encoder.writeObject(thing);
            byte[] bytes = baos.toByteArray();
            return new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (encoder != null) {
                    encoder.close();
                }
            } catch (Exception e) {
            }
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static void toFile(Object thing, String path) {
        if (path.endsWith(".xml")) {
            XMLEncoder encoder = null;
            try {
                encoder = new XMLEncoder(new BufferedOutputStream(
                        new FileOutputStream(path)));
                encoder.writeObject(thing);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (encoder != null) {
                    encoder.close();
                }
            }
        } else {
            ObjectOutputStream encoder = null;
            try {
                encoder = new ObjectOutputStream(new BufferedOutputStream(
                        new FileOutputStream(path)));
                encoder.writeObject(thing);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (encoder != null) {
                    try {
                        encoder.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public static void delete(String absPath, boolean recurse) {
        File file = new File(absPath);
        if (file.exists()) {
            if (recurse && file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; files != null && i < files.length; i++) {
                        if (files[i].isDirectory() && recurse) {
                            delete(files[i].getAbsolutePath(), recurse);
                        }
                        files[i].delete();
                    }
                }
            }
            file.delete();
        }
    }

    public static <T> T fromFile(String path, Class<T> tType) {
        try {
            return fromStream(path.endsWith(".xml"), new BufferedInputStream(
                    new FileInputStream(path)), tType);
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static <T> T fromStream(boolean isXml, InputStream is, Class<T> tType) {
        if (isXml) {
            XMLDecoder decoder = null;
            try {
                decoder = new XMLDecoder(new BufferedInputStream(is));
                return (T) decoder.readObject();
            } catch (Exception e) {
                if (e instanceof java.io.InvalidClassException) {
                    log.info("got type mismatch");
                } else {
                    e.printStackTrace();
                }
            } finally {
                if (decoder != null) {
                    decoder.close();
                    decoder = null;
                    System.gc();
                }
            }
        } else {
            ObjectInputStream decoder = null;
            try {
                decoder = new ObjectInputStream(new BufferedInputStream(
                        is));
                return (T) decoder.readObject();
            } catch (Exception e) {
                if (e instanceof java.io.InvalidClassException) {
                    log.info("got type mismatch");
                } else {
                    e.printStackTrace();
                }
            } finally {
                if (decoder != null) {
                    try {
                        decoder.close();
                    } catch (Exception e) {
                    }
                    decoder = null;
                    System.gc();
                }
            }
        }

        return (T) (List.class.isAssignableFrom(tType) ? Collections.<T>emptyList()
                : Set.class.isAssignableFrom(tType) ? Collections.<T>emptySet()
                        : Map.class.isAssignableFrom(tType) ? Collections.emptyMap()
                        : null);
    }


    /**
     * 
     * @param path path to device in question
     * @return the free space (normalized) left on device accessible by path
     */
    public static double freeSpaceN(String path) {
        try {
            
            if (System.getProperty("os.name").startsWith("Windows") ) {
                return -1;
            }

            Exec exec = new Exec();
            String cmd[] = { "df", path };
            int status = exec.command(cmd);
            Process p = exec.getProcess();

            if (status == 0) {
                String buff = exec.getOutputGobbler().output();
                int pctIdx = buff.lastIndexOf("%");
                if(pctIdx > 3) {
                    int startIdx = pctIdx - 3;
                    int endIdx = pctIdx;
                    String pct = buff.substring(startIdx, endIdx);
                    return 1. - (Double.parseDouble(pct) / 100.);
                } else {
                    return -1.;
                }
            } else {
                System.out.println(exec.getErrorGobbler().output());
                return -1.;
            }
        } catch(java.lang.StringIndexOutOfBoundsException ignored) { 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1.;
    }


    public static String mkPath(String... frags) {
        StringBuilder sb = new StringBuilder();
        sb.append(File.separator);
        int lead,trail;
        String lastFrag = "";
        for(String p : frags) {
            lastFrag = p;
            p = p.trim();
            lead = p.startsWith(File.separator) ? 1 : 0;
            trail = p.endsWith(File.separator) ? 1 : 0;
            if(lead + trail < p.length()) {
                sb.append(p.substring(lead, p.length() -trail));
            }
            sb.append(File.separator);
        }
        if(lastFrag.length() > 0 && !lastFrag.endsWith(File.separator) ) {
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    

}
