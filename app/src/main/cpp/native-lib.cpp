
#include <iostream>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <unistd.h>
#include <queue>
#include <jni.h>

using namespace std;

#define MAXMSGSIZE 1024
#define MAXNAMESIZE 30
#define SERVERIP "121.164.238.137"
#define PORT 2693

enum Protocol
{
    PT_ENTER = 0,    // 대화방에 입장하기..
    PT_TALK,	 // 이야기 하기..
    PT_EXIT,	  // 대화방 나가기..
    PT_DISCONNECT,  // 강제로 접속 끊고 나가기..
    PT_LOGINED,     // 이미 접속하고 있다..
    PT_USERLIST
};

struct CommonMessage
{
    int  Protocol;           // 프로토콜 번호..
    char UserID[MAXNAMESIZE];// 유저 아이디..
    char Message[MAXMSGSIZE];// 메시지..
};

int serverSocket = -1;
struct sockaddr_in serverAddr;
fd_set read_fds, tmp_fds;
int fileDesc;
char userName[MAXNAMESIZE];
ssize_t readedLength;
ssize_t ret;

CommonMessage sendMsg, readMsg;

queue < std::string > UserMessageQueue;
queue < std::string > ServerMessageQueue;
queue < std::string > UserListQueue;

char totalMessage[MAXNAMESIZE + MAXMSGSIZE + 10];


std::string JStringToString(JNIEnv *env, jstring in)
{
    char *nativeStr;
    const char *temp = env->GetStringUTFChars(in, 0);
    nativeStr = strdup(temp);
    env->ReleaseStringUTFChars( in, temp );

    return string(nativeStr);
}

void SetUserMessage( std::string msg ) // 서버로 보낼 메시지를 큐에 저장한다..
{
    UserMessageQueue.push( msg );
}

extern "C" JNIEXPORT void JNICALL
Java_com_rootjm_roottalks_RootTalkService_SetUserMessage(
        JNIEnv *env, jobject, jstring message ) {

    std::string sMsg = JStringToString( env, message );

    SetUserMessage( sMsg );
}

std::string GetUserMessage(void)  //서버로 보낼 메시지 큐에서 메시지를 꺼내온다..  // 요기까지 밥먹구 이따가하자!! =.=;;
{
    long size = UserMessageQueue.size();

    if( size>0 ) {

        std::string sMsg = UserMessageQueue.front();

        UserMessageQueue.pop();

        return sMsg;
    }
    else {
        return "";
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_rootjm_roottalks_RootTalkService_GetUserMessage(
        JNIEnv *env, jobject ) {

    std::string msg = GetUserMessage();

    return env->NewStringUTF( msg.c_str() );
}

// !!
void SetUserList( std::string userId )
{
    UserListQueue.push( userId );
}

extern "C" JNIEXPORT void JNICALL
Java_com_rootjm_roottalks_RootTalkService_SetUserList(
        JNIEnv *env, jobject, jstring userid ) {

    std::string sUserId = JStringToString( env, userid );

    SetUserList( sUserId );
}

std::string GetUserList(void)
{
    long size = UserListQueue.size();

    if( size>0 ) {

        std::string sUserId = UserListQueue.front();

        UserListQueue.pop();

        return sUserId;
    }
    else {
        return "";
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_rootjm_roottalks_RootTalkService_GetUserList(
        JNIEnv *env, jobject ) {

    std::string sUserId = GetUserList();

    return env->NewStringUTF( sUserId.c_str() );
}
// !!

void SetServerUserMessage(std::string sMsg)// const char *message)  //서버에서 받은 메시지를 큐에 저장한다..
{
    ServerMessageQueue.push( sMsg );
}

extern "C" JNIEXPORT void JNICALL
Java_com_rootjm_roottalks_RootTalkService_SetServerUserMessage(
        JNIEnv *env, jobject, jstring message )
{
    std::string sMsg = JStringToString( env, message );

    SetServerUserMessage( sMsg );
}


std::string GetServerUserMessage(void)  // 서버에서 받은 메시지를 저장한 큐에서 메시지를 가져온다..
{
    long size = ServerMessageQueue.size();

    if( size>0 )
    {
        std::string sMsg = ServerMessageQueue.front();

        ServerMessageQueue.pop();

        return sMsg;

    } else {

        return "";
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_rootjm_roottalks_RootTalkService_GetServerUserMessage(
        JNIEnv *env, jobject )
{
    std::string msg = GetServerUserMessage();

    return env->NewStringUTF( msg.c_str() );
}


int ConnectToServer(void) //-----------------------------------1
{
    // 서버에 접속할 소켓을 생성한다..
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);

    if( serverSocket<0 )
    {
        printf("클라이언트 소켓을 생성하는데 실패했습니다..\n");
        return 1;
    }

    // 서버의 주소와 포트를 설정한다.
    memset( &serverAddr, 0, sizeof(serverAddr));

    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = inet_addr(SERVERIP);  // 서버의 ip 를 설정한다..
    serverAddr.sin_port = htons(PORT); // 서버의 포트 번호를 설정한다..

    // 서버에 접속한다..
    ret = connect( serverSocket,(struct sockaddr *)&serverAddr, sizeof(serverAddr));

    if( ret<0 ) {
        printf("서버에 접속하는데 실패했습니다..\n");
        return 1;
    }

    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_rootjm_roottalks_RootTalkService_ConnectToServer(
        JNIEnv *env, jobject )
{
    int i =  ConnectToServer();

    return i;
}

void SetUserName(const char *pUserName) //----------------------------------2
{
    strcpy( userName, pUserName);
   // userName[strlen(userName)-1] = '\0';
}

extern "C" JNIEXPORT void JNICALL
Java_com_rootjm_roottalks_RootTalkService_SetUserName(
        JNIEnv *env, jobject, jstring username )
{
    const char *pUsername = env->GetStringUTFChars( username, 0 );

    SetUserName(pUsername);

    env->ReleaseStringUTFChars( username, pUsername );
}

int SendUserIDToServer(void) //-------------------------------------------3
{
    // 서버에 유저 아이디 정보를 보내서 중복되지 않으면 서버에 저장한다..
    memset(&sendMsg, 0, sizeof(sendMsg));
    sendMsg.Protocol = PT_ENTER;
    strcpy( sendMsg.UserID, userName );
    strcpy( sendMsg.Message, "클라이언트의 아이디 정보를 서버에서 받았습니다..");

    // 서버에 지정된 메시지를 보낸다..
    ret = write( serverSocket, &sendMsg, (ssize_t ) sizeof(sendMsg));

    if( ret<0 )
    {
        printf("서버에 메시지를 전달하는데 실패했습니다..\n");
        return 1;
    }

    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_rootjm_roottalks_RootTalkService_SendUserIDToServer(
        JNIEnv *env, jobject )
{
    int i = SendUserIDToServer();

    return i;
}



void InitSocketSets(void) //---------------------------------------------4
{
    FD_ZERO(&tmp_fds);  // temp 셋 초기화..
    FD_ZERO(&read_fds);  // 읽기셋 초기화..

    FD_SET( serverSocket, &read_fds );  // 서버셋을 읽기셋에 셋한다..
    FD_SET( 0, &read_fds );  // 0 을 읽기 셋에 셋한다..

    fileDesc = serverSocket;  // 서버 소켓 번호를 받아둔다..
}

extern "C" JNIEXPORT void JNICALL
Java_com_rootjm_roottalks_RootTalkService_InitSocketSets(
        JNIEnv *env, jobject )
{
    InitSocketSets();
}

int ProcessCommunication(void) //-----------------------------------------5
{
    tmp_fds = read_fds;  // 읽기셋을 임시셋에 집어넣는다..

    ret = select(fileDesc+1, &tmp_fds, 0, 0, 0); // Select 실행한다..

    if(ret<0) {
        printf("Select 에러 입니다..\n");
        return 1;
    }

    if(FD_ISSET(serverSocket, &tmp_fds))  //서버 소켓의 변화가 있다면 동작한다..
    {
        // 서버 소켓에서 클라이언트에 보낸 메시지를 읽어들인다..
        memset( &readMsg, 0, sizeof(readMsg) );

        readedLength = read( serverSocket, &readMsg, (size_t) sizeof(readMsg));  // 요기서 에러가남.. 음수값이 나온다..!!

        if(readedLength<0) {  // 만일 읽어들인 길이가 음수이면 읽기 에러가 난 것이다..
            printf("Read Error: \n");
            return 1;
        }
        else if(readedLength==0) { // 만일 읽어들인 길이가 0 이면 서버와의 접속이 끊긴것이다..
            printf("서버와의 접속이 끊겼습니다..\n");  // !!! 오늘은 요기까지.. 서버와의 접속이 끊길 때의 처리를 해주자..!!!
            return 1;
        }
        // 읽어들인 메시지를 표시한다..
        switch(readMsg.Protocol)  // 서버에서 받아둔 메시지를 표시한다..
        {
            case PT_TALK: // 메시지의 프로토콜이 일반 채팅이면 그것을 표시해 준다..
            {
                memset( totalMessage, 0, sizeof(totalMessage));

                sprintf( totalMessage,"[%s] %s", readMsg.UserID, readMsg.Message );

                SetServerUserMessage( string(totalMessage) );
            }
            break;

            case PT_EXIT: // 메시지의 프로토콜이 중복된 아이디가 존재한다면 그것을 표시해 주고 유저 아이디 입력 받는 곳으로 간다..
            {
                printf("[%s] %s \n", readMsg.UserID, readMsg.Message );

            }
            break;

            case PT_USERLIST:  // 유저 아이디를 큐에 저장한다..
            {
                SetUserList( readMsg.UserID );
            }
            break;
        }
    }
    else if(FD_ISSET(0, &tmp_fds))  //  메시지를 서버로 전송한다..
    {
       for( int i=0; i<UserMessageQueue.size(); i++ ) // 서버로 전송할 메시지큐가 차 있으면 빌 때까지 보낸다..
       {
           // 메시지를 버퍼에 받아둔다..
            memset(&sendMsg, 0, sizeof(sendMsg));
            sendMsg.Protocol = PT_TALK;
            strcpy(sendMsg.UserID, userName);
            strcpy(sendMsg.Message, GetUserMessage().c_str() );

            // 타이핑한 메시지를 서버에 전송한다..
            ret = write(serverSocket, &sendMsg, (size_t)sizeof(sendMsg));

            if (ret < 0) { // 만일 리턴값이 0 보다 작으면 서버 쓰기 에러가 난 것이니 종료한다..
                printf("Server Writing Error!! \n");
                return 1;
            }
        }
    }

    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_rootjm_roottalks_RootTalkService_ProcessCommunication(
        JNIEnv *env, jobject )
{
    int i = ProcessCommunication();

    return i;
}

void CloseSocket(void) //---------------------------------------------6
{
    if(serverSocket != -1) {
        close(serverSocket);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_rootjm_roottalks_RootTalkService_CloseSocket(
        JNIEnv *env, jobject )
{
    CloseSocket();
}

int QueryUserListFromServer(void)
{
    // 서버에 유저 아이디 정보를 보내서 중복되지 않으면 서버에 저장한다..
    memset(&sendMsg, 0, sizeof(sendMsg));

    sendMsg.Protocol = PT_USERLIST;
    strcpy( sendMsg.UserID, userName );
    strcpy( sendMsg.Message, "클라이언트에게서 사용자 리스트를 요청했습니다..");

    // 서버에 지정된 메시지를 보낸다..
    ret = write( serverSocket, &sendMsg, (ssize_t ) sizeof(sendMsg));

    if( ret<0 )
    {
        printf("서버에 메시지를 전달하는데 실패했습니다..\n");
        return 1;
    }

    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_rootjm_roottalks_RootTalkService_QueryUserListFromServer(
        JNIEnv *env, jobject )
{
    int i = QueryUserListFromServer();

    return i;
}

int GetUserListCount(void)
{
    int count = UserListQueue.size();

    return count;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_rootjm_roottalks_RootTalkService_GetUserListCount(
        JNIEnv *env, jobject )
{
    return GetUserListCount();
}

/*
 int SendUserIDToServer(void) //-------------------------------------------3
{
    // 서버에 유저 아이디 정보를 보내서 중복되지 않으면 서버에 저장한다..
    memset(&sendMsg, 0, sizeof(sendMsg));
    sendMsg.Protocol = PT_ENTER;
    strcpy( sendMsg.UserID, userName );
    strcpy( sendMsg.Message, "클라이언트의 아이디 정보를 서버에서 받았습니다..");

    // 서버에 지정된 메시지를 보낸다..
    ret = write( serverSocket, &sendMsg, (ssize_t ) sizeof(sendMsg));

    if( ret<0 )
    {
        printf("서버에 메시지를 전달하는데 실패했습니다..\n");
        return 1;
    }

    return 0;
}
 */