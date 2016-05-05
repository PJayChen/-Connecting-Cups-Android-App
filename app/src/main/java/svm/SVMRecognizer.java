package svm;

import android.content.Context;
import android.util.Log;

import org.apache.http.entity.ByteArrayEntity;

import libsvm.*;
import svm.svm_predict;
import svm.svm_scale;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import server.FeatureVector;

public class SVMRecognizer {

	static final String TAG = "SVM";
	static Context context = null;

	public static String motionTypes[] = {"shakeV", "shakeH", "drinking", "swaying", "toasting", "unknown"};
	static boolean append = false; // create new file or overwrite exist file.
	private static void toLibsvm_format(double[] feature_series, String motionType, String outFileName) {
		int label = 0;

		FileOutputStream fos;
//		FileWriter fw;
		
		// translate motion type to a integer as label
		for (int i = 0; i < motionTypes.length; i++) {
			if (motionType.equals(motionTypes[i])) {
				label = i;
				break;
			}
		}
		
		try {
			if (append == true) {
				fos = context.openFileOutput(outFileName, Context.MODE_APPEND);
//				fw = new FileWriter(outFileName, true);
			} else {
				fos = context.openFileOutput(outFileName, Context.MODE_PRIVATE);
//				fw = new FileWriter(outFileName);
				append = true;
			}

			fos.write(String.format("%d ", label).getBytes());
//			fw.write(String.format("%d ", label));
			
			for (int i=0; i<feature_series.length; i++) {
				if (feature_series[i] == 0) continue;
				fos.write(String.format("%d:%f ", i+1, feature_series[i]).getBytes());
//				fw.write(String.format("%d:%f ", i+1, feature_series[i]));
			}

			fos.write("\n".getBytes());
			fos.close();
//			fw.write("\n");
//			fw.flush();
//			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private static void featureExtraction(List<List<FeatureVector>> motionFramesList) {
		
			// converts feature vector into a array and writes into file in libsvm tf format.
			double[] z, m, uM_Frames, uZ_Frames;
			int j=0,a=0;
			double meanM = 0, varM = 0, meanM_frame = 0, uT_m = 0,lastMeanM_frame=0, uD_m=0;
			double meanZ = 0, varZ = 0, meanZ_frame = 0, uT_z = 0,lastMeanZ_frame=0, uD_z=0;
			
			z = new double[motionFramesList.size() * motionFramesList.get(0).size()];
			m = new double[motionFramesList.size() * motionFramesList.get(0).size()];
			uM_Frames = new double[motionFramesList.size() * motionFramesList.get(0).size()];
			uZ_Frames = new double[motionFramesList.size() * motionFramesList.get(0).size()];
			
			for (List<FeatureVector> motionFrame : motionFramesList) {
				for (FeatureVector fv: motionFrame) {
					m[j] = Math.abs(fv.getAcceleration_magnitude());
					z[j++] = fv.getAcceleration_z();
					
					meanZ += fv.getAcceleration_z();
					meanM += Math.abs(fv.getAcceleration_magnitude());
					
					meanM_frame += Math.abs(fv.getAcceleration_magnitude());
					meanZ_frame += Math.abs(fv.getAcceleration_z());
				}
				meanM_frame /= motionFrame.size();
				meanZ_frame /= motionFrame.size();
				
				uT_m += Math.abs(meanM_frame - lastMeanM_frame);
				uT_z += Math.abs(meanZ_frame - lastMeanZ_frame);
				
				lastMeanM_frame = meanM_frame;
				lastMeanZ_frame = meanZ_frame;
				
				uM_Frames[a] = meanM_frame;
				uZ_Frames[a++] = meanZ_frame;
				
				meanM_frame = 0;
				meanZ_frame = 0;
			}
			// mean of Z and magnitude of entire motion sequence.
			meanZ = meanZ / (motionFramesList.size() * motionFramesList.get(0).size());
			meanM = meanM / (motionFramesList.size() * motionFramesList.get(0).size());
			
			for (double _z: z) {
				varZ += (meanZ - _z)*(meanZ - _z);
			}
			varZ /= z.length;
			
			for (double _m: m) {
				varM += (meanM - _m)*(meanM - _m);
			}
			varM /= m.length;
			
			for (double uM: uM_Frames) {
				uD_m += Math.abs(meanM - uM);
			}
			uD_m /= uM_Frames.length;
			
			for (double uZ: uZ_Frames) {
				uD_z += Math.abs(meanZ - uZ);
			}
			uD_z /= uZ_Frames.length;
			
			double[] feature_series = {meanZ, meanM, varM, varZ, uT_m, uT_z};
			
			toLibsvm_format(feature_series, "unknown", "testingSet.tf");
				
		append = false; // for toLibsvm_format()
	}
	
	// invokes binary execution file to do SVM classification.
	static public void identifyMotionB(List<List<FeatureVector>> motionFramesList) {
		String scaling = "./svm-scale -r templates.tf.range testingSet.tf > testingSet.tf.scale";
		String predict = "./svm-predict testingSet.tf.scale templates.tf.model testingSet.tf.predict";
		
		String[] cmd = {"sh", "-c","cd ./libsvm/ && " + scaling + "&&" + predict}; 
		
		String s;
		
		// feature extraction and translate to LIBSVM format.		
		featureExtraction(motionFramesList);
		
		try {			
			// scale and predict.
			Process p = Runtime.getRuntime().exec(cmd);			
			
//			BufferedReader stdInput = new BufferedReader(new
//	                 InputStreamReader(p.getInputStream()));
//			
//			// read the output from the command
//	        System.out.println("Here is the standard output of the command:\n");
//	        while ((s = stdInput.readLine()) != null) {
//	            System.out.println(s);
//	        }
			
			// wait for writing testingSet.tf.predict file.
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        BufferedReader input = new BufferedReader(new FileReader("./libsvm/testingSet.tf.predict"));
	        String line = input.readLine();
	        System.out.printf("[SVM] Detect: %s\n", motionTypes[Integer.valueOf(line)]);
	        input.close();
	        
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	// invokes Java version SVM classification.
	static public void identifyMotionJ(List<List<FeatureVector>> motionFramesList, Context ctxt) {
		// templates.tf.range and templates.tf.model are in the Assets which are read only.
		// testingSet.tf.scale, testingSet.tf and testingSet.tf.predict are in the Internal Storage.
		String[] scaling = {"-r", "libsvm/templates.tf.range", "-o", "testingSet.tf.scale", "testingSet.tf"};
		String[] predict = {"-q", "testingSet.tf.scale", "libsvm/templates.tf.model", "testingSet.tf.predict"};

		context = ctxt;

		// feature extraction and translate to LIBSVM format.
		// store the testingSet.tf in internal storage.
		featureExtraction(motionFramesList);


		// scale and predict.
		try {
			svm_scale.main(scaling, context);

			svm_predict.main(predict, context);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
