#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <md5.h>
#define PNAME_CAID_PNAME 0
#define PNAME_CAID_CAID 1
#define BLOCKS_COUNT 9
#define BLOCK_LEN 34000
#define STR_MAX 128
#define _DEBUG_

int printBlock(char * blockStart, int blockLen){
    int i = 0;
    int j = 0;
    unsigned char c = 0;
    for(i = 0; i < blockLen; i++){
        c = * (blockStart + i);
        for(j = 0; j < 8; j++){
            printf("%d", (c >> j) & 0x01);
        }
        if(i % 2 == 1){
            printf(" ");
        }
        if(i % 16 == 15){
            printf("\n");
        }
    }
    printf("\n");
    return 0;
}

int setBit(char * blockStart, int blockLen, int key, char value){
    blockStart[key / 8] |= (0x01 & value) << (key % 8);
}

int getBit(char * blockStart, int blockLen, int key){
    return (blockStart[key / 8] >> (key % 8)) & 0x01;
}

int strMd5(char * in_str, char * out_str){
    MD5_CTX md5;
    MD5Init(&md5);         
    int i = 0;
    for(i = 0; i < 16; i++){
        out_str[i] = 0;
    }
    MD5Update(&md5, in_str, strlen((char *)in_str));
    MD5Final(&md5, out_str);        
}

int printHex(char * block, int len){
    int i = 0;
    for(i = 0; i < len; i++){
        printf("%02x", 0xff & block[i]);
    }
}

int strMd5fp(char * str, unsigned int * md5fp){
    unsigned char pname_md5[16];
    strMd5(str, pname_md5);
    int i = 0;
    for(i = 0 ; i < 5; i++){
        md5fp[i] = * (unsigned int *) &pname_md5[i * 3];
    }
}

int str2Block(char * str, char * block, int blockLen){
    unsigned int md5fp[5];
    strMd5fp(str, md5fp);
    int i = 0;
    for(i = 0; i < 5; i++){
        setBit(block, blockLen, md5fp[i] % (blockLen * 8), 1);
    }
}

int strInBlock(char * str, char * block, int blockLen){
    short exists = 1;
    unsigned int md5fp[5];
    strMd5fp(str, md5fp);
    int i = 0;
    for(i = 0; i < 5; i++){
        if(!getBit(block, blockLen, md5fp[i] % (blockLen * 8))){
            exists = 0;
            break;
        }
    }
    return exists;
}

int echo(int num){
    printf("%d", num);
}

int initBlocks(unsigned char blocks[][BLOCK_LEN], int blocksCount, int blockLen, char * csv_path){
    int i = 0;
    int j = 0;
    unsigned char * block = 0;
    for(i = 0; i < 8; i++){
        block = blocks[i];
        for(; j < blockLen; j++){
            block[j] = 0;
        }
    }

    int caid = 0;
    char pname[STR_MAX];
    char count[STR_MAX];
    int pos_comm = 0;
    if(!csv_path){
        csv_path = "./bloom.csv";
    }
    FILE * fp = fopen(csv_path, "r");
    if(!fp){
        return 0;
    }
    int limit_line = 100000;
    while(!feof(fp) && limit_line--){
        fscanf(fp, "\"%[^\"]\",\"%d\",%s\n", pname, &caid, count);
        block = blocks[caid];
        str2Block(pname, block, blockLen);
    }
    return 1;
}

int strInBlocks(char * str, char blocks[][BLOCK_LEN], int blocksCount, int blockLen){
    int i = 0;
    int found_caid = -1;
    short check_caid = 0;
    char exists_order[] = {1, 7, 2, 3, 4, 5, 6, 0, 8};
    for(i = 0; i < blocksCount; i++){
        check_caid = exists_order[i];
        if(strInBlock(str, blocks[check_caid], blockLen)){
            found_caid = check_caid;
            break;
        }
    }
    return found_caid;
}

int saveBlocks(char * blocks, int len, char * block_file){
    FILE * fp;
    fp = fopen(block_file, "w+");
    if(!fp){
        return -1;
    }
    fwrite(blocks, sizeof(char), len, fp);
    fclose(fp);
    return 1;
}

int loadBlocks(char blocks[][BLOCK_LEN], int len, char * block_file){
    FILE * fp;
    fp = fopen(block_file, "r");
    if(!fp){
        return -1;
    }
    fread(blocks, sizeof(char), len, fp);
    fclose(fp);

    return 1;
}

char * createBlock(){
    unsigned char blocks[BLOCKS_COUNT][BLOCK_LEN];
    if(1 != initBlocks(blocks, BLOCKS_COUNT, BLOCK_LEN, (char *)0)){
        printf("initBlocks error\n");
        return 0;
    }
    printf("initBlocks done\n");
    if(1 != saveBlocks((char *)blocks, BLOCK_LEN * BLOCKS_COUNT, "blocks")){
        printf("saveBlocks error\n");
        return 0;
    }
    int i = 0;
    for(i = 0; i < BLOCKS_COUNT; i++){
        printBlock((char *) blocks[i], 48);
    }
    printf("saveBlocks done\n");
}

long cate_bloom_init(char * block_path){
    char (*blocks)[BLOCK_LEN];
    blocks = (char(*)[BLOCK_LEN])malloc(BLOCK_LEN * BLOCKS_COUNT);
    printf("sizeof rb %d\n", sizeof(*blocks));
    if(!block_path){
        block_path = "./blocks";
    }
    if(1 != loadBlocks(blocks, BLOCK_LEN * BLOCKS_COUNT, block_path)){
#ifdef _DEBUG_
        printf("readBlocks fail\n");
#endif
        return -1;
    }
#ifdef _DEBUG_
    printf("readBlocks succ\n");
#endif
    return (long)blocks;
}

int cate_bloom_caid(long blocks, char * pname){
    int caid = -1;
    caid = strInBlocks(pname, (char(*)[BLOCK_LEN])blocks, BLOCKS_COUNT, BLOCK_LEN);
    if(caid >= 0){
        caid += 800;
    }
    return caid;
}

void cate_bloom_free(long blocks){
    free((char(*)[BLOCK_LEN])blocks);
}


int main(){
    // 制造
    /* createBlock(); */

    // 初始化
    long pCo = cate_bloom_init(NULL);

    // 使用
    char * catenames[] = {"生活", "新闻", "沟通", "影音", "阅读", "工具", "美化", "金融", "游戏"};
    short found_caid = -1;
    char catename[STR_MAX];
    char pname[STR_MAX];
    while(1){
        scanf("%s", pname);
        found_caid = cate_bloom_caid(pCo, pname);
        if(found_caid < 0){
            memset(catename, 0, STR_MAX);
            strcpy(catename, "未知");
        }else{
            memset(catename, 0, STR_MAX);
            strcpy(catename, catenames[found_caid]);
        }
        printf("found_caid: %d - %s\n", found_caid, catename);
    }

    // 释放
    cate_bloom_free(pCo);
    getchar();
    return 0;
}
