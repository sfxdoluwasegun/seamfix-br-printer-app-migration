package com.seamfix.brprinterapp.FargoPrinter;

import com.seamfix.brprinterapp.utils.AlertUtils;
import impl.Fargo.*;

@SuppressWarnings("PMD")
public class MakeCardOperation {
    // Name of the printer
    private String m_printerName;

    // Current Activity of the printer
    private CurrentActivity m_currentActivity;

    // Current job number
    private int m_jobNumber = 0;

    // Not already connected
    private boolean m_connected = false;

    /**
     * The m_start sentinels.
     */
    private String[] m_startSentinels = new String[3];

    /**
     * The m_stop sentinels.
     */
    private String[] m_stopSentinels = new String[3];


    // Object to do the work.
    //private FargoPrinter m_fargoPrinter;
    private PrintJob m_printJob;

    // PrinterInfo object to read the Current Activity.
    private PrinterInfo m_printerInfo;

    // Constructor
    MakeCardOperation(String printerName) {
        // Copy the name of the printer.
        m_printerName = printerName;

        // EACH THREAD MUST CREATE ITS OWN COPY OF THE PrintJob OBJECT.
        m_printJob = new PrintJob(printerName);


        // Create the PritnerInfo object used to get the CurrentActivity.
        m_printerInfo = new PrinterInfo(printerName);
    }


    /**
     * Sets the start sentinel.
     *
     * @param trackNumber Track number (1 to 3).
     * @param value       the ASCII value of the start sentinel for that track (for ISO
     *                    this is "%" on track 1, ";" for tracks 2 and 3.)
     *                    <p>
     *                    <p>Supported Printers</p>
     *                    <ul>
     *                    <li>HDP8500</li>
     *                    <li>HDP5000/HDPii (pre 2013)</li>
     *                    <li>HDP5000/HDPii-HDPiiplus (post 2013)</li>
     *                    <li>DTC1000/1000M/4000/4500</li>
     *                    <li>DTC1000Me/1250e/4250e/4500e</li>
     *                    </ul>
     */
    public void setStartSentinel(int trackNumber, String value) {
        // Magnetic track start and stop sentinels
        m_startSentinels[trackNumber - 1] = value;
    }

    /**
     * Sets the stop sentinel.
     *
     * @param trackNumber Track number (1 to 3).
     * @param value       the ASCII value of the stop sentinel for that track (for ISO
     *                    this is "?")
     *                    <p>Supported Printers</p>
     *                    <ul>
     *                    <li>HDP8500</li>
     *                    <li>HDP5000/HDPii (pre 2013)</li>
     *                    <li>HDP5000/HDPii-HDPiiplus (post 2013)</li>
     *                    <li>DTC1000/1000M/4000/4500</li>
     *                    <li>DTC1000Me/1250e/4250e/4500e</li>
     *                    </ul>
     */
    public void setStopSentinel(int trackNumber, String value) {
        // Magnetic track start and stop sentinels
        m_stopSentinels[trackNumber - 1] = value;
    }

    /**
     * Gets the start sentinel.
     *
     * @param trackNumber Track number (1 to 3).
     * @return the start sentinel used for this track.
     * <p>Supported Printers</p>
     * <ul>
     * <li>HDP8500</li>
     * <li>HDP5000/HDPii (pre 2013)</li>
     * <li>HDP5000/HDPii-HDPiiplus (post 2013)</li>
     * <li>DTC1000/1000M/4000/4500</li>
     * <li>DTC1000Me/1250e/4250e/4500e</li>
     * </ul>
     */
    public String getStartSentinel(int trackNumber) {
        // Magnetic track start and stop sentinels
        return (m_startSentinels[trackNumber - 1]);
    }

    /**
     * Gets the stop sentinel.
     *
     * @param trackNumber Track number (1 to 3).
     * @return the stop sentinel used for this track.
     * <p>Supported Printers</p>
     * <ul>
     * <li>HDP8500</li>
     * <li>HDP5000/HDPii (pre 2013)</li>
     * <li>HDP5000/HDPii-HDPiiplus (post 2013)</li>
     * <li>DTC1000/1000M/4000/4500</li>
     * <li>DTC1000Me/1250e/4250e/4500e</li>
     * </ul>
     */
    public String getStopSentinel(int trackNumber) {
        // Magnetic track start and stop sentinels
        return (m_stopSentinels[trackNumber - 1]);
    }


    /**
     * Create the card
     */
    public void produceCard() {
        // Open the printer, getting the print job object to pass back to native DLL.
        // This creates the m_printer instance of the FargoPrinter class.
        openConnection(m_printerName);

        // Get the printer status.
        m_currentActivity = m_printerInfo.currentActivity();

        if (m_currentActivity == CurrentActivity.CurrentActivityReady) {
            // Do something because it is ready.
            m_printJob.setCoercivity(Coercivity.Low);
        }

        // Set the coercivity to something else
        m_printJob.setCoercivity(Coercivity.High);

        // Add the elements to the print job.
        addElements();

        // *******************************************
        // Now print all of the print job elements
        // *******************************************

        // Cause the print job to be created with the elements sent.
        printCard();

        // Close the printer.
        closeConnection();
    }


    private void addElements() {

        // First need to set the start / stop sentinels for mag encoding.
        // In this case use ISO.
        setStartSentinel(1, "%");
        setStartSentinel(2, ";");
        setStartSentinel(3, ";");
        setStopSentinel(1, "?");
        setStopSentinel(2, "?");
        setStopSentinel(3, "?");


        // For face 0=front, 1=back

        // *******************************************
        // Image data elements
        // *******************************************

        // Print picture HERE using the file name (JPG)
//        m_printJob.addPrintImageElement( /*m_pictureDirectorty + m_pictureName*/"frontland.jpg", 1, 0, 0, 0);
//        m_printJob.addPrintImageElement( /*m_pictureDirectorty + m_pictureName*/"backport.jpg", 1, 0, 0, 1);
//

        m_printJob.addPrintImageElement( /*m_pictureDirectorty + m_pictureName*/"printImages/front.jpg", 1, 0, 0, 0);
        m_printJob.addPrintImageElement( /*m_pictureDirectorty + m_pictureName*/"printImages/back.jpg", 1, 0, 0, 1);


        // *******************************************
        // Text data elements
        // *******************************************

        // Add text to front
//		m_printJob.addPrintTextElement( "The quick brown fox", 10, 10, 0, "Arial", PrintJob.FontStyles.FONT_ATTRIBUTE_ITALIC, 20, PrintJob.FontColors.FONT_COLOR_DKRED);

        //// Add text to back
//		m_printJob.addPrintTextElement( "jumps over the lazy",  10, 10, 1, "Arial", PrintJob.FontStyles.FONT_ATTRIBUTE_BOLD, 25, PrintJob.FontColors.FONT_COLOR_BLACK);


        // *******************************************
        // F-Panel or I-Panel data (sent as text data)
        // *******************************************

        // Add F-Panel to front side
        //m_printer.addPrintTextElement( "~Ic:\\front_v.bmp", 0, 0, 0, "Arial", PrintJob.FontStyles.FONT_ATTRIBUTE_ITALIC, 1, PrintJob.FontColors.FONT_COLOR_BLACK);
        //m_printer.addPrintTextElement( "~TThis Is a BigIhello", 10, 50, 0, "Arial", 0, 20, PrintJob.FontColors.FONT_COLOR_BLACK);

        //// Add I-Panel to back side
        //m_printer.addPrintTextElement( "~ic:\\back.bmp", 0, 0, 1, "Arial", PrintJob.FontStyles.FONT_ATTRIBUTE_ITALIC, 1, PrintJob.FontColors.FONT_COLOR_BLACK);


        // *******************************************
        // Mag data elements
        // *******************************************


        // Add sample mag data
        for (int index = 0; index < 3; index++) {
            // get string data for this track
            String trackData = null;

            // Just need something.
            switch (index + 1) {
                // Track 1 -
                case 1:
                    trackData = "JULIE ANDERSON^623-85-1253";
                    break;
                // Track 2 -
                case 2:
                    trackData = "0123456789";
                    break;
                case 3:
                default:
                    trackData = "0123456789";
                    break;
            }


            // Do we have data for this track?
//            if (trackData != null) {

//                // encode the track data
//                String trackEncoded = getStartSentinel(index + 1) +
//                        trackData +
//                        getStopSentinel(index + 1);
//
//                // print the current Data Input Element
//                m_printJob.addPrintMagElement(trackEncoded, index + 1);
//            }
        }

    }


    /**
     * Print the card
     */
    private void printCard() {

        // Bump spool job name
        m_jobNumber++;

        // Do the Print Job.  This starts the job and the associated pages, then ends the pages and the job.
        //
        // Note that the XML file has the printer instance name in it so this needs to be consistent with the
        // printer instance this object was created with.
        String xmlFileName = "testboth.xml";
        m_printJob.doComboJob("Sample Card Number " + m_jobNumber, xmlFileName);

        //// Or can do the default - which is split ribbon on, landscape, auto ribbon select
//		m_printer.doComboJob("Sample Card Number " + m_jobNumber, (String)null);

        // Finish the document if no more elements
        finishIfNoMore();
    }


    // Finish the document if no more elements to be sent to the print job.
    private void finishIfNoMore() {
        // Finish the document if needed
        // This waits until the CurrentActivity indicates either
        // an error, printer to ready or unknown (communication lost)
        m_currentActivity = m_printJob.finishDoc();

        // If there is an exception on the printer then must throw it to the application
        if (m_currentActivity != CurrentActivity.CurrentActivityReady) {
            // What kind of problem was it?
            switch (m_currentActivity) {
                // Printer error
                case CurrentActivityException:

                    // Determine the type of the exception
                    PrinterError resultPrinterError = m_printerInfo.printerError();

                    switch (resultPrinterError) {
                        case PrinterErrorMagNoData:
                        case PrinterErrorMagNone:
                        case PrinterErrorMagVerifier:
                            break;

                        case PrinterErrorCardFeed:
                        case PrinterErrorCardJam:
                        case PrinterErrorCardJamEject:
                        case PrinterErrorCardJamEncoder:
                        case PrinterErrorCardJamFlipper:
                        case PrinterErrorCardJamLaminator:
                            break;

                        default:
                            break;
                    } // end switch on printer error type

                    break;

                // Lost communication
                case CurrentActivityUnknown:
                default:
                    break;
            } // end case of CurrentActivity result

        } // end if the CurrentActivity was not ready.
    }


    /**
     * Opens a connection to the FargoPrinter object
     *
     * @param name
     * @return
     */
    private boolean openConnection(String name) {

        boolean result = true;

        // If already connected then do not allow connection.
        if (m_connected) {
            // generate exception
            result = false;
        }

        m_connected = true;


        // attempt to open the print job
        if (!m_printJob.open(name)) {
            // We failed to "open" the print job!
            // generate exception
            AlertUtils.getError("Failed to open the Print job").show();
            return false;
        }

        // Attempt to get the printer status.  If the printer is NOT communicating
        // then must throw an "not reachable" exception.
        m_currentActivity = m_printerInfo.currentActivity();
        if (m_currentActivity == CurrentActivity.CurrentActivityUnknown) {
            // The printer is not communicating.
            // generate exception
            AlertUtils.getError("Failed to communicate with the Printer").show();
            result = false;
        }

        return (result);
    } // end openConnection


    /**
     * Close the connection to the printing object.
     */
    private void closeConnection() {

        // Attempt to get the printer status.  If the printer is NOT communicating
        // then must throw an "not reachable" exception.
        m_currentActivity = m_printerInfo.currentActivity();
        if (m_currentActivity == CurrentActivity.CurrentActivityUnknown) {
            // The printer is not communicating.
            // generate exception
        }


        // attempt to close the print job
        if (!m_printJob.close()) {
            // We failed to close the printer!
            // generate exception
        }

        m_connected = false;
    }

}
