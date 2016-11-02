/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -----------------------------------------------------------
*
*  TarsosDSP is developed by Joren Six at 
*  The Royal Academy of Fine Arts & Royal Conservatory,
*  University College Ghent,
*  Hoogpoort 64, 9000 Ghent - Belgium
*  
*  http://tarsos.0110.be/tag/TarsosDSP
*  https://github.com/JorenSix/TarsosDSP
*  http://tarsos.0110.be/releases/TarsosDSP/
* 
*/
package be.hogent.tarsos.dsp.example;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.SilenceDetector;
import be.hogent.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.hogent.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.hogent.tarsos.dsp.mfcc.MFCC;
import be.hogent.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
/**
 * Provides support for different types of command line audio feature extraction.
 * @author Joren Six
 */
public class FeatureExtractor {
	
	private final List<FeatureExtractorApp> featureExtractors;
	
	public FeatureExtractor(String...arguments){
		//Create a list of feature extractors
		featureExtractors = new ArrayList<FeatureExtractorApp>();
		featureExtractors.add(new SoundPressureLevelExtractor());
		featureExtractors.add(new PitchExtractor());
		featureExtractors.add(new RootMeanSquareExtractor());
		
		checkArgumentsAndRun(arguments);
	}
	
	private void checkArgumentsAndRun(String... arguments){
		if(arguments.length == 0){
			printError();
		} else {
			String subCommand = arguments[0].toLowerCase();
			FeatureExtractorApp appToExecute = null;
			for(FeatureExtractorApp app : featureExtractors){
	    		if(subCommand.equalsIgnoreCase(app.name())){
	    			appToExecute = app;	
	    		}
	    	}
			if(appToExecute == null){
				printError();
			}else{
				try {
					if(!appToExecute.run(arguments)){
						printHelp(appToExecute);
					}
				} catch (UnsupportedAudioFileException e) {
					printHelp(appToExecute);
					printLine();
					System.err.println("Error:");
					System.err.println("\tThe audio file is not supported!");
				} catch (IOException e) {
					printHelp(appToExecute);
					printLine();
					System.err.println("Current error:");
					System.err.println("\tIO error, maybe the audio file is not found or not supported!");
				}
			}
		}
	}
	
	private final void printError(){
		printPrefix();
		System.err.println("Name:");
		System.err.println("\tTarsosDSP feature extractor");
		printLine();
		System.err.println("Synopsis:");
		System.err.println("\tjava -jar FeatureExtractor.jar SUB_COMMAND [options...]");
		printLine();
		System.err.println("Description:");
		System.err.println("\t Extracts features from an audio file, SUB_COMMAND needs\n\tto be one of the following:");
		for (FeatureExtractorApp app : featureExtractors) {
			System.err.println("\t\t" + app.name());
		}
    }
	
	private final void printPrefix(){
		 System.err.println(" _______                       _____   _____ _____  ");
		 System.err.println("|__   __|                     |  __ \\ / ____|  __ \\ ");
		 System.err.println("   | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |");
		 System.err.println("   | |/ _` | '__/ __|/ _ \\/ __| |  | |\\___ \\|  ___/ ");
		 System.err.println("   | | (_| | |  \\__ \\ (_) \\__ \\ |__| |____) | |     ");
		 System.err.println("   |_|\\__,_|_|  |___/\\___/|___/_____/|_____/|_|     ");
		 System.err.println("                                                    ");
		 printLine();
	}
	
	public static void printLine(){
		System.err.println("----------------------------------------------------");
	}
	
	private final void printHelp(FeatureExtractorApp appToExecute){
		printPrefix();
		System.err.println("Name:");
		System.err.println("\tTarsosDSP " + appToExecute.name() + " feature extractor");
		printLine();
		System.err.println("Synopsis:");
		System.err.println("\tjava -jar FeatureExtractor.jar " + appToExecute.name() + " " + appToExecute.synopsis());
		printLine();
		System.err.println("Description:");
		System.err.println(appToExecute.description());

    }

	/**
	 * @param arguments
	 */
	public static void main(String... arguments) {
		new FeatureExtractor(arguments);
	}
	
	private interface FeatureExtractorApp{
		String name();
		String description();
		String synopsis();
		boolean run(String... args) throws UnsupportedAudioFileException, IOException;
		
	}
	
	private class RootMeanSquareExtractor implements FeatureExtractorApp{

		@Override
		public String name() {
			return "rms";
		}

		@Override
		public String description() {
			return "\tCalculates the root mean square of an audio signal for each \n\tblock of 2048 samples. The output gives you a timestamp and the RMS value,\n\tSeparated by a semicolon.\n\n\t\n\ninput.wav: a\treadable audio file.";
		}

		@Override
		public String synopsis() {
			return "input.wav";
		}

		@Override
		public boolean run(String... args) throws UnsupportedAudioFileException, IOException {
			if(args.length!=2){
				return false;
			}

			String inputFile = args[1];
            System.out.println(inputFile);



			int sampleRate = 44100;
			int bufferSize = 1024;
			int bufferOverlap = 128;
            int lengthInSamples = 4096;
            String file = inputFile;
            final float[] buffer = audioBufferFile(file,lengthInSamples);
//			final float[] floatBuffer = TestUtilities.audioBufferSine();
//			final double sampleRate = 44100.0;
//			final double f0 = 440.0;
//			final double amplitudeF0 = 0.5;
//			final double seconds = 4.0;
//			final float[] buffer = new float[(int) (seconds * sampleRate)];
//			for (int sample = 0; sample < buffer.length; sample++) {
//				final double time = sample / sampleRate;
//				buffer[sample] = (float) (amplitudeF0 * Math.sin(2 * Math.PI * f0 * time));
//			}

			final AudioDispatcher dispatcher = AudioDispatcherFactory.fromFloatArray(buffer, sampleRate, bufferSize, bufferOverlap);
			final MFCC mfcc = new MFCC(bufferSize, sampleRate, bufferOverlap);
			dispatcher.addAudioProcessor(mfcc);
			dispatcher.addAudioProcessor(new AudioProcessor() {

				@Override
				public void processingFinished() {

				}

				@Override
				public boolean process(AudioEvent audioEvent) {

                    System.out.println("hello");
//                    float[] mfcc1 = mfcc.getMFCC();
//                    for(int i =0; i<mfcc1.length;++i){
//                        System.out.println(mfcc1[i]);
//                    }
                    return true;
				}
			});
			dispatcher.run();
            return true;
		}
	}

    public static float[] audioBufferFlute() {
        int lengthInSamples = 4096;
        String file = "/be/tarsos/dsp/test/resources/flute.novib.ff.A4.wav";
        return audioBufferFile(file,lengthInSamples);
    }

    private static float[] audioBufferFile(String file,int lengthInSamples){
        float[] buffer = new float[lengthInSamples];
        try {
            File audioFile = new File(file);
//            final URL url = FeatureExtractor.class.getResource();
            System.out.println(audioFile.toURI().toURL());
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile.toURI().toURL());
            AudioFormat format = audioStream.getFormat();
            TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter.getConverter(JVMAudioInputStream.toTarsosDSPFormat(format));
            byte[] bytes = new byte[lengthInSamples * format.getSampleSizeInBits()];
            audioStream.read(bytes);
            converter.toFloatArray(bytes, buffer);
        } catch (IOException e) {
            throw new Error("Test audio file should be present.");
        } catch (UnsupportedAudioFileException e) {
            throw new Error("Test audio file format should be supported.");
        }
        return buffer;
    }
	
	private class SoundPressureLevelExtractor implements FeatureExtractorApp{

		@Override
		public String name() {
			return "sound_pressure_level";
		}

		@Override
		public String description() {
			return "\tCalculates a sound pressure level in dB for each\n\tblock of 2048 samples.The output gives you a timestamp and a value in dBSPL.\n\tSeparated by a semicolon.\n\n\t\n\nWith input.wav\ta readable audio file.";
		}

		@Override
		public String synopsis() {
			return "input.wav";
		}

		@Override
		public boolean run(String... args) throws UnsupportedAudioFileException, IOException {
			if(args.length!=2){
				return false;
			}
				
			String inputFile = args[1];
			File audioFile = new File(inputFile);
			int size = 2048;
			int overlap = 0;
			final SilenceDetector silenceDetecor = new SilenceDetector();		
			AudioDispatcher dispatcher = AudioDispatcher.fromFile(audioFile, size, overlap);
			dispatcher.addAudioProcessor(silenceDetecor);
			dispatcher.addAudioProcessor(new AudioProcessor() {
				@Override
				public void processingFinished() {
				}
				
				@Override
				public boolean process(AudioEvent audioEvent) {
					System.out.println(audioEvent.getTimeStamp() + ";" + silenceDetecor.currentSPL());
					return true;
				}
			});
			dispatcher.run();
			return true;
		}
	}
	
	private class PitchExtractor implements FeatureExtractorApp, PitchDetectionHandler{

		@Override
		public String name() {
			return "pitch";
		}

		@Override
		public String description() {
			String descr = "\tCalculates pitch in Hz for each block of 2048 samples. \n\tThe output is a semicolon separated list of a timestamp, frequency in hertz and \n\ta probability which describes how pitched the sound is at the given time. ";
			descr += "\n\n\tinput.wav\t\ta readable wav file.";
			descr += "\n\t--detector DETECTOR\tdefaults to FFT_YIN or one of these:\n\t\t\t\t";
			for(PitchEstimationAlgorithm algo : PitchEstimationAlgorithm.values()){
				descr += algo.name() + "\n\t\t\t\t";
			}
			return descr;
		}

		@Override
		public String synopsis() {
			String helpString = "[--detector DETECTOR] input.wav";			
			return helpString;
		}

		@Override
		public boolean run(String... args) throws UnsupportedAudioFileException, IOException {
			PitchEstimationAlgorithm algo = PitchEstimationAlgorithm.FFT_YIN;
			String inputFile = args[1];
			
			if(args.length == 1 || args.length == 3){
				return false;
			}else if(args.length==4 && !args[1].equalsIgnoreCase("--detector")){
				return false;
			}else if(args.length==4 && args[1].equalsIgnoreCase("--detector")){
				try{
					algo = PitchEstimationAlgorithm.valueOf(args[2].toUpperCase());
					inputFile = args[3];
				}catch(IllegalArgumentException e){
					//if enum value string is not recognized
					return false;
				}
			}
			File audioFile = new File(inputFile);
			float samplerate = AudioSystem.getAudioFileFormat(audioFile).getFormat().getSampleRate();
			int size = 1024;
			int overlap = 0;
			AudioDispatcher dispatcher = AudioDispatcher.fromFile(audioFile, size, overlap);
			dispatcher.addAudioProcessor(new PitchProcessor(algo, samplerate, size, this));
			dispatcher.run();
			return true;
			
		}

		@Override
		public void handlePitch(PitchDetectionResult pitchDetectionResult,
				AudioEvent audioEvent) {
			double timeStamp = audioEvent.getTimeStamp();
			float pitch = pitchDetectionResult.getPitch();
			float probability = pitchDetectionResult.getProbability();
			System.out.println(timeStamp+";"+pitch+";"+probability);
		}
	}
	
	

}
