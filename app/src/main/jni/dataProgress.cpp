#include "dataProgress.h"


dataProgress::dataProgress()
{
}


dataProgress::~dataProgress()
{
	delete data;
	delete id;
	delete pure_ids;
}
void dataProgress::init() {
	id=new int[id_num];
	pure_ids=new int[id_num];
	for(int i=0;i<id_num;i++){
		id[i]=getID(getFrame(i));
		if(id[i]>=0x110&&id[i]<0x400){
			pure_ids[i]=(id[i]-0x100)/0x10;
		}else{
			pure_ids[i]=-1;
		}

		/**
		 *  if(id[i]>0x480){
		 id[i]=0x000;
		 PureIDs[i]=0x000;
		 }
		 if(id[i]<0x100){
		 PureIDs[i]=0x000;
		 }
		 */
		if(id[i]==0x55){
			pure_ids[i]=0x00;
		}

		//Log.v("dataTag","pureID"+PureIDs[i]+"  "+id[i]);
	}
	//data=getFrameData(frame);
}
int* dataProgress::getId() {
	return id;
}

int dataProgress::getIdNum() {
	return id_num;
}

int* dataProgress::getPureIds() {
	return pure_ids;
}
char* dataProgress::getFrame(int xu) {
	char* frame=new char[FRAMELENGTH];
	for (int i = 0; i < FRAMELENGTH; i++) {
		frame[i] = data[xu*FRAMELENGTH + i];
	}
	return frame;
}
void dataProgress::setFrame(char *data, int num) {
	if (!allow_set_data) {
		return;
	}
	if (this->data != NULL) {
		delete this->data;
	}
	data = new char[num];
	this->data = data;
	this->data_byte_num = num;
	id_num=num/20;
	allow_set_data = false;
	init();
}
void dataProgress::setAllowSetNewData(){
	allow_set_data = true;
}

int dataProgress::getID(char *frame) {
	char a = frame[6];
	char b = frame[7];
	int id = ((a & 0xff) * 256) + (b & 0xff);
	return id;
}

int dataProgress::getLcuTempNum(int moduleId) {

	int num = 0;
	for (int i = 0; i<id_num; i++) {
		int lId = getID(getFrame(i));
		//LCU的id范围为1-40模块，如需更改，请在下面的判断句里面更改
		if (lId>0x100 && lId<0x38f) {
			lId = lId & 0x00f;//如果是LCU发来的数据，取最后4位看看是哪些数据
		}
		else {
			return -1;
		}
		if ((lId >> 4) - 0x10 == moduleId) {
			if (lId == 0x007 || lId == 0x008) {
				dataNode dataTemp = getFrameFloatData(i);
				num += dataTemp.num;
			}
			else {
				return -1;
			}
		}
		else {
			return -1;
		}



	}

	return num;
}

char* dataProgress::getFrameData(char* Frame) {
	char* data = new char[8];
	for (int i = 8; i<16; i++) {
		data[i - 8] = Frame[i];
	}
	return data;
}

dataNode dataProgress::getFrameFloatData(int numOfFrame) {
	int MaskedI = 0;
	dataNode *result = new dataNode;
	data = getFrameData(getFrame(numOfFrame));
	//lecu的数据
	if (id[numOfFrame]>0x100 && id[numOfFrame]<0x400) {
		//对ID末位数据进行判断
		MaskedI = (id[numOfFrame] & 0x00f);
		switch (MaskedI) {
		case 0x000://单个电池组温度和电压信息
			result->data = new float[4];//依次为：cellMaxVoltage;cellMinVoltage;cellMaxTemp;cellMinTemp
			result->num = 4;
			if ((data[1] >> 7) == 0) {
				(result->data)[0] = 9999;//表示如果无效为6
			}
			else {
				(result->data)[0] = ((float)((data[0] & 0xff) + (data[1] & 0x7f) * 0xff)) / 1000;
			}
			if ((data[3] >> 7) == 0) {
				(result->data)[1] = 9999;//表示如果无效为6
			}
			else {
				(result->data)[1] = ((float)((data[2] & 0xff) + (data[3] & 0x7f) * 0xff)) / 1000;
			}
			if ((data[5] >> 7) == 0) {
				(result->data)[2] = 9999;//表示如果无效为6
			}
			else {
				(result->data)[2] = ((float)((data[4] & 0xff) + (data[5] & 0x07) * 0xff)) / 10 - 40;
			}
			if ((data[7] >> 7) == 0) {
				(result->data)[3] = 9999;//表示如果无效为6
			}
			else {
				(result->data)[3] = ((float)((data[6] & 0xff) + (data[7] & 0x07) * 0xff)) / 10 - 40;
			}



			break;
		case 0x002://报警信息
			result->data = new float[12];
			result->num = 12;
			(result->data)[0] = ((float)((data[0] & 0xff) + (data[1] & 0x7f) * 0xff)) / 1000;//平均电压
			(result->data)[1] = ((float)((data[2] & 0xff) + (data[3] & 0x07) * 0xff)) / 10 - 40;//平均温度
																								//此数据帧信息还有8个，如果需要可以处理
			(result->data)[2] = 0;
			(result->data)[3] = 0;
			(result->data)[4] = 0;
			(result->data)[5] = 0;
			(result->data)[6] = 0;
			(result->data)[7] = 0;
			(result->data)[8] = 0;
			(result->data)[9] = 0;
			(result->data)[10] = 0;
			(result->data)[11] = 0;
			break;
		case 0x004:
		case 0x005:
		case 0x006:
		case 0x00A:
		case 0x00B:
		case 0x00C://单体电池信息
			result->data = new float[4]; 
			result->num = 4;
			for (int i = 0; i<4; i++) {
				if ((data[2 * i + 1] >> 7) == 0) {
					(result->data)[i] = 9999;//表示如果无效为6
					(result->num) --;
				}
				else {
					(result->data)[i] = ((float)((data[2 * i] & 0xff) + (data[2 * i + 1] & 0x7f) * 0xff)) / 1000;
				}
			}
			break;
		case 0x007:
		case 0x008://单体电池温度探头信息
			result->data = new float[4];
			result->num = 4;
			for (int i = 0; i<4; i++) {
				if ((data[2 * i + 1] >> 7) == 0) {
					(result->num)--;
					(result->data)[i] = 9999;//如果无效全部设置为60
				}
				else {
					(result->data)[i] = ((float)((data[2 * i] & 0xff) + (data[2 * i + 1] & 0x07) * 0xff) - 400) / 10;
				}
			}
			break;
		case 0x009:
			result->data = new float[3];
			result->num = 3;
			for (int i = 0; i<3; i++) {
				(result->data)[i] = data[i] & 0xff;
			}
			break;
		}
	}
	else if (id[numOfFrame]<0x100 || id[numOfFrame]>0x400) {
		switch (id[numOfFrame]) {
		case 0x50:
			//暂不处理
			break;
		case 0x51:
			//暂不处理
			break;
		case 0x52:
			//暂不处理
			break;
		case 0x53:
			//暂不处理
			break;
		case 0x54:
			//此帧包含BMU最大电压、最小电压和平均电压
			result->data = new float[3];
			result->num = 3;
			(result->data)[0] = ((float)((data[0] & 0xff) * 16 + ((data[1] & 0xf0) >> 4))) / 1000;
			(result->data)[1] = ((float)((data[1] & 0x0f) * 256 + (data[2] & 0xff))) / 1000;
			(result->data)[2] = ((float)((data[3] & 0xff) * 16 + ((data[4] & 0xf0) >> 4))) / 1000;
			break;
		case 0x55:
			//此帧包含BMU最高温度、最低温度和平均温度
			result->data = new float[3];
			result->num = 3;
			(result->data)[0] = ((float)((data[0] & 0xff) * 16 + ((data[1] & 0xf0) >> 4))) / 10 - 40;
			(result->data)[1] = ((float)((data[1] & 0x0f) * 256 + (data[2] & 0xff))) / 10 - 40;
			(result->data)[2] = ((float)((data[3] & 0xff) * 16 + ((data[4] & 0xf0) >> 4))) / 10 - 40;
			break;
		case 0x56:
			break;
		case 0x57:
			break;
		case 0x411:
			//此帧包含电流，绝缘阻抗，电池端电压，负载端电压，剩余容量
			//Log.v("dataTag","0:"+(int)(data[0]&0xff)+"  \n"+(int)(data[1]&0xff)+"\n"+(data[2]&0xff)+"\n"+(data[3]&0xff));
			result->data = new float[5];
			result->num = 5;
			(result->data)[0] = ((float)((data[0] & 0xff) * 256 + (data[1] & 0xff))) / 40.0 - 1000.0;
			(result->data)[1] = ((float)((data[2] & 0xff) * 256 + (data[3] & 0xff))) / 4.0;
			(result->data)[2] = ((float)(((data[4] & 0xff) * 16 + ((data[5] & 0xf0) >> 4)))) / 4.0;
			(result->data)[3] = ((float)(((data[5] & 0x0f) * 256 + (data[6] & 0xff)))) / 4.0;
			(result->data)[4] = ((float)(data[7] & 0xff)) / 2;
			//Log.v("dataTag","0:"+result[0]+"  \n"+result[1]+"\n"+result[2]+"\n"+result[3]);
			break;
		case 0x413:
			//此帧包含继电器状态
			// Log.v("dataTag","1:"+(data[0]&0xff));
			result->data = new float[2];
			result->num = 2;
			(result->data)[0] = data[0] & 0x30 >> 4;
			(result->data)[1] = data[0] & 0x0c >> 2;
			//Log.v("dataTag","0:"+result[0]+"  \n"+result[1]);
			break;
		case 0x415:
			break;
		case 0x416:
			//处理相关的电压最值模块单体位置信息
			result->data = new float[8];
			result->num = 8;
			(result->data)[0] = data[0] >> 4 & 0x0f;
			(result->data)[1] = data[0] & 0x0f;
			(result->data)[2] = data[1] >> 4 & 0x0f;
			(result->data)[3] = data[1] & 0x0f;
			(result->data)[4] = data[2] >> 4 & 0x0f;
			(result->data)[5] = data[2] & 0x0f;
			(result->data)[6] = data[3] >> 4 & 0x0f;
			(result->data)[7] = data[3] & 0x0f;
			break;
		case 0x417:
			result->data = new float[1];
			result->num = 1;
			(result->data)[0] = data[6] & 0xff;
			break;
		default:
			break;

		}
	}
	return *result;
}
