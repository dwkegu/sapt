#include"com_psf_sapt_wifiService_DataProgress.h"

#ifdef __cplusplus
extern "C"{
#endif
	void init() {
		mdata = new dataProgress();
		has_init = true;
	}

	void end() {
		if (mdata != NULL) {
			delete mdata;
			has_init = false;
		}
	}

	JNIEXPORT jintArray JNICALL Java_com_psf_sapt_wifiService_DataProgress_getId
	(JNIEnv *env, jobject jobject) {
		if (!has_init) {
			return NULL;
		}
		int idNum = mdata->getIdNum();
		jintArray result = env->NewIntArray(idNum);
		int* id = mdata->getId();
		jint* ids = new jint[idNum];
		for (int i = 0; i < idNum; i++) {
			ids[i] = id[i];
		}
		//jint* ids=env->getIn
		env->SetIntArrayRegion(result, 0, idNum, ids);
		delete ids;
		return result;
	}
	JNIEXPORT jintArray JNICALL Java_com_psf_sapt_wifiService_DataProgress_getPureIDs
	(JNIEnv *env, jobject jobject) {
		if (!has_init) {
			return NULL;
		}
		int idNum = mdata->getIdNum();
		jintArray result = env->NewIntArray(idNum);
		int* id = mdata->getPureIds();
		jint* ids = new jint[idNum];
		for (int i = 0; i < idNum; i++) {
			ids[i] = id[i];
		}
		//jint* ids=env->getIn
		env->SetIntArrayRegion(result, 0, idNum, ids);
		delete ids;
		return result;

	}

	JNIEXPORT jbyteArray JNICALL Java_com_psf_sapt_wifiService_DataProgress_getFrame
	(JNIEnv *env, jobject jobject, jint i) {
		if (!has_init) {
			return NULL;
		}
		char * data= mdata->getFrame(i);
		jbyte* _data = (jbyte*)data;
		jbyteArray result = env->NewByteArray(FRAMELENGTH);
		env->SetByteArrayRegion(result, 0, FRAMELENGTH, _data);
		return result;

	}

	JNIEXPORT void JNICALL Java_com_psf_sapt_wifiService_DataProgress_setFrames
	(JNIEnv *env, jobject jobject, jbyteArray frame) {
		if (!has_init) {
			init();
		}
		char* data = (char*)env->GetByteArrayElements(frame, false);
		int num = env->GetArrayLength(frame);
		mdata->setFrame(data, num);
		env->ReleaseByteArrayElements(frame, (jbyte*)data, 0);
	}
	JNIEXPORT void JNICALL Java_com_psf_sapt_wifiService_DataProgress_setAllowSetNewData
	(JNIEnv *env, jobject jobject) {
		if (!has_init) {
			return;
		}
		mdata->setAllowSetNewData();
	}

	JNIEXPORT jint JNICALL Java_com_psf_sapt_wifiService_DataProgress_getID
	(JNIEnv *env, jobject jobject, jbyteArray frame) {
		if (!has_init) {
			return 0;
		}
		char* data = (char*)env->GetByteArrayElements(frame, false);
		return mdata->getID(data);

	}

	JNIEXPORT jint JNICALL Java_com_psf_sapt_wifiService_DataProgress_getLcuTempNum
	(JNIEnv *env, jobject jobject, jint id) {
		if (mdata == NULL) {
			return -1;
		}
		int _id = mdata->getLcuTempNum(id);


		return _id;
	}

	JNIEXPORT jbyteArray JNICALL Java_com_psf_sapt_wifiService_DataProgress_getFrameData
	(JNIEnv *env, jobject jobeject, jbyteArray frame) {
		if (mdata == NULL) {
			return NULL;
		}
		char* _frame = (char*)env->GetByteArrayElements(frame, false);
		char* _result=mdata->getFrameData(_frame);
		jbyteArray result = env->NewByteArray(8);
		env->SetByteArrayRegion(result, 0, 8, (jbyte*)_result);
		env->ReleaseByteArrayElements(frame, (jbyte*)_frame, 0);
		return result;
	}

	JNIEXPORT jfloatArray JNICALL Java_com_psf_sapt_wifiService_DataProgress_getFrameFloatData
	(JNIEnv *env, jobject jobject, jint id) {
		if (mdata == NULL) {
			return NULL;
		}
		dataNode _result = mdata->getFrameFloatData(id);
		jfloatArray result= env->NewFloatArray(_result.num);
		env->SetFloatArrayRegion(result, 0, _result.num, _result.data);
		return result;
	}



	JNIEXPORT void JNICALL Java_com_psf_sapt_wifiService_DataProgress_end
	(JNIEnv *env, jobject jobject) {
		end();
	}


#ifdef __cplusplus
}
#endif