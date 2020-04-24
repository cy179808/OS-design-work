package com.company;


import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class Get_information {      //测试Oshi用法

    private static SystemInfo info = new SystemInfo();

    public static void OS() {
        OperatingSystemMXBean OS = ManagementFactory.getOperatingSystemMXBean();
        System.out.println("----------------操作系统信息----------------");
        System.out.println("操作系统名：" + OS.getName());
        System.out.println("操作系统版本" + OS.getVersion());
        System.out.println("操作系统架构：" + OS.getArch());
    }

    public static void CPU() throws InterruptedException {
        CentralProcessor processor = info.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        //让系统睡眠一秒
        TimeUnit.SECONDS.sleep(1);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        System.out.println("----------------cpu信息----------------");
        System.out.println("cpu核数:" + processor.getLogicalProcessorCount());
        System.out.println("cpu系统使用率:" + new DecimalFormat("#.##%").format(cSys * 1.0 / totalCpu));
        System.out.println("cpu用户使用率:" + new DecimalFormat("#.##%").format(user * 1.0 / totalCpu));
        System.out.println("cpu当前等待率:" + new DecimalFormat("#.##%").format(iowait * 1.0 / totalCpu));
        System.out.println("cpu当前使用率:" + new DecimalFormat("#.##%").format(1.0 - (idle * 1.0 / totalCpu)));
    }


    public static void Memory() {
        GlobalMemory memory = info.getHardware().getMemory();
        //总内存
        long totalMemory = memory.getTotal();
        //剩余内存
        long availableMemory = memory.getAvailable();
        System.out.println("----------------内存信息----------------");
        System.out.println("总内存：" + formatByte(totalMemory));
        System.out.println("使用内存：" + formatByte(totalMemory - availableMemory));
        System.out.println("剩余内存：" + formatByte(availableMemory));
        System.out.println("实时占用率: " + new DecimalFormat("#.##").format((totalMemory - availableMemory) * 1.0 / totalMemory));
    }

    public static String formatByte(long byteNumber) {
        //换算单位
        double FORMAT = 1024.0;
        double kbNumber = byteNumber / FORMAT;
        if (kbNumber < FORMAT) {
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber / FORMAT;
        if (mbNumber < FORMAT) {
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber / FORMAT;
        if (gbNumber < FORMAT) {
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber / FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
    }

    public static void Disk() {
        System.out.println("----------------硬盘信息----------------");
        HWDiskStore[] diskStores = info.getHardware().getDiskStores();
        if (diskStores != null && diskStores.length > 0) {
            for (HWDiskStore diskStore : diskStores) {
                String name = diskStore.getName();
                String id = diskStore.getSerial();
                long size = diskStore.getSize();
                long writtenSize = diskStore.getWriteBytes();
                System.out.println("硬盘名：" + name);
                System.out.println("硬盘ID：" + id);
                System.out.println("硬盘总容量： " + formatByte(size));
                System.out.println("使用率：" + new DecimalFormat("#.##").format((size - writtenSize) * 1.0 / size));
            }
        }
    }




}
