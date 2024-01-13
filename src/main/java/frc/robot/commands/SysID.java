// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import static edu.wpi.first.units.MutableMeasure.mutable;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.Volts;

import java.util.Collection;
import java.util.List;

import org.javatuples.LabelValue;

import edu.wpi.first.units.Distance;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.MutableMeasure;
import edu.wpi.first.units.Velocity;
import edu.wpi.first.units.Voltage;
import edu.wpi.first.wpilibj.sysid.SysIdRoutineLog;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Config;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.subsystems.Subsystems;
import frc.robot.util.SwerveModuleVoltages;

/** A utility class to create SysID logging commands. */
public class SysID {
    private static final MutableMeasure<Voltage> appliedVoltage = mutable(Volts.of(0));
    private static final MutableMeasure<Distance> distance = mutable(Meters.of(0));
    private static final MutableMeasure<Velocity<Distance>> velocity = mutable(MetersPerSecond.of(0));

    /**
     * Returns the commands to characterize the swerve drive.
     * 
     * @param subsystems The subsystems container.
     * @return The commands to characterize the swerve drive.
     */
    public Collection<LabelValue<String, Command>> getSwerveDriveCharacterizationCommands(Subsystems subsystems) {
        SysIdRoutine.Config routineConfig = new SysIdRoutine.Config();
        SysIdRoutine.Mechanism mechanism = new SysIdRoutine.Mechanism(
            (Measure<Voltage> volts) -> {
                SwerveModuleVoltages voltages = new SwerveModuleVoltages(volts.in(Volts), 0.0);
                SwerveModuleVoltages[] moduleVoltages = new SwerveModuleVoltages[] {
                    voltages, voltages, voltages, voltages
                };
                subsystems.drivetrain.setModuleVoltages(moduleVoltages);
                appliedVoltage.mut_replace(volts);
            }, 
            (SysIdRoutineLog log) -> {
                log.motor("Drive")
                    .voltage(appliedVoltage)
                    .linearPosition(distance.mut_replace(subsystems.drivetrain.getPosition().getX(), Meters))
                    .linearVelocity(velocity.mut_replace(subsystems.drivetrain.getChassisSpeeds().vxMetersPerSecond, MetersPerSecond));

            }, 
            subsystems.drivetrain);
        SysIdRoutine routine = new SysIdRoutine(routineConfig, mechanism);
        return List.of(
            new LabelValue<String, Command>("Swerve Drive Quasistatic Forward", routine.quasistatic(Direction.kForward)), 
            new LabelValue<String, Command>("Swerve Drive Quasistatic Reverse", routine.quasistatic(Direction.kReverse)),
            new LabelValue<String, Command>("Swerve Drive Dynamic Forward", routine.dynamic(Direction.kForward)),
            new LabelValue<String, Command>("Swerve Drive Dynamic Reverse", routine.dynamic(Direction.kReverse))
        );
    }
}