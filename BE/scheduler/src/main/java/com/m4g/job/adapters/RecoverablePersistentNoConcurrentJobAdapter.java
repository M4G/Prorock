package com.m4g.job.adapters;

import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class RecoverablePersistentNoConcurrentJobAdapter extends RecoverablePersistentJobAdapter {
}
