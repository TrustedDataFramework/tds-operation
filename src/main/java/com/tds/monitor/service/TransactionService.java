package com.tds.monitor.service;

import com.tds.monitor.utils.ApiResult.APIResult;


public interface TransactionService {
    APIResult getTxrecordFromAddress(String address);
}
