/*
 * Copyright (c) [2018]
 * This file is part of the java-wisdomcore
 *
 * The java-wisdomcore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The java-wisdomcore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the java-wisdomcore. If not, see <http://www.gnu.org/licenses/>.
 */

package com.tds.monitor.model;



import com.tds.monitor.utils.KeystoreUtil;

import java.security.PublicKey;


public class Address {

    //hex string,not include 0x prefix
    private  String address;
    public static final byte numb = 0x00;


    /*
    1. 地址生成逻辑

    2. 对公钥进行SHA3-256计算，获得结果为s1

    3. 取得s1的后面22字节，并且在前面附加3个字符（WXC，大写字符），共25字节，结果为s2
    */
    public  String pubkeyToAddress(PublicKey publicKey){
        return KeystoreUtil.pubkeyToAddress(publicKey.getEncoded(),numb);
    }

    public Address(){

    }

    public Address(PublicKey publicKey){
        this.address = pubkeyToAddress(publicKey);
    }

    public String getAddress() {
        return address;
    }


}