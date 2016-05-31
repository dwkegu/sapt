#pragma once
#ifndef DATAPROGRESS_H
#define DATAPROGRESS_H
#define FRAMELENGTH 20
#define NULL 0
struct dataNode {
	int num;
	float* data;
};
class dataProgress
{
public:
	dataProgress();
	void init();
	int* getId();
	int getIdNum();
	int* getPureIds();
	char* getFrame(int i);
	void setFrame(char *data,int num);
	void setAllowSetNewData();
	int getLcuTempNum(int);
	int getID(char*);
	dataNode getFrameFloatData(int);
	char* getFrameData(char*);
	~dataProgress();
private:
	char *data;
	int data_byte_num;
	int* id;
	int id_num;
	int* pure_ids;
	bool allow_set_data;

};

#endif
