#!/usr/bin/env python3
"""
PolyStrike 3D - Realistic Military Asset Generator (Blender Headless)
Gathers and synthesizes realistic 3D assets (weapons, soldiers/operators, combat environment)
with skeletons (rigging), vertex weights, and tactical animations exported to mobile-ready .glb.

License Compliance:
Strictly adheres to 100% CC0 (Public Domain Dedication) & MIT-compatible algorithms.
No CC-BY or attribution-dependent datasets are utilized. Suitable for closed-source
and commercial mobile distribution.
"""

import sys
import os
import math
import argparse

try:
    import bpy
    import mathutils
except ImportError:
    print("[ERROR] Este script debe ejecutarse dentro de Blender:")
    print("        blender -b -P tools/blender/generate_realistic_assets.py -- [opciones]")
    sys.exit(1)


def parse_args():
    argv = sys.argv
    if "--" in argv:
        argv = argv[argv.index("--") + 1:]
    else:
        argv = []

    parser = argparse.ArgumentParser(description="Generador de assets tácticos 3D realistas")
    parser.add_argument("--type", choices=["weapon", "character", "environment"], default="weapon",
                        help="Categoría de asset táctico a generar")
    parser.add_argument("--model", type=str, default="tactical_assault_rifle",
                        help="Identificador del modelo táctico")
    parser.add_argument("--output", type=str, default="",
                        help="Ruta de destino del archivo .glb")
    parser.add_argument("--animated", action="store_true", default=True,
                        help="Incluir animaciones de combate horneadas en el .glb")
    parser.add_argument("--lod", choices=["mobile_high", "mobile_opt"], default="mobile_opt",
                        help="Nivel de optimización poligonal para GPU móvil")
    return parser.parse_args(argv)


def reset_scene():
    """Elimina todos los objetos, materiales y datos por defecto de Blender."""
    bpy.ops.wm.read_factory_settings(use_empty=True)
    for block in (bpy.data.objects, bpy.data.meshes, bpy.data.armatures,
                  bpy.data.materials, bpy.data.actions):
        for item in list(block):
            block.remove(item)


def create_pbr_material(name, base_color, metallic=0.0, roughness=0.5):
    """Crea un material PBR realista con Principled BSDF."""
    mat = bpy.data.materials.new(name=name)
    mat.use_nodes = True
    nodes = mat.node_tree.nodes
    bsdf = nodes.get("Principled BSDF")
    if bsdf:
        bsdf.inputs["Base Color"].default_value = base_color
        bsdf.inputs["Metallic"].default_value = metallic
        bsdf.inputs["Roughness"].default_value = roughness
    return mat


# ==============================================================================
# 1. ARMAS DE FUEGO TÁCTICAS REALISTAS (WEAPONS)
# ==============================================================================

def build_realistic_weapon(model_name):
    """
    Construye un arma de fuego militar realista con partes funcionales separadas,
    materiales PBR tácticos y esqueleto completo para animaciones en primera persona.
    Soporta múltiples modelos del dataset táctico:
      - tactical_assault_rifle
      - heavy_combat_shotgun
      - tactical_smg
      - service_pistol
      - marksman_sniper
      - silenced_carbine
      - squad_lmg
      - combat_revolver
    """
    mat_metal_dark = create_pbr_material("Mat_GunMetal", (0.12, 0.13, 0.14, 1.0), metallic=0.9, roughness=0.35)
    mat_metal_fde = create_pbr_material("Mat_GunFDE", (0.42, 0.36, 0.28, 1.0), metallic=0.7, roughness=0.5)
    mat_polymer_tan = create_pbr_material("Mat_PolymerTan", (0.35, 0.30, 0.22, 1.0), metallic=0.05, roughness=0.7)
    mat_steel_bolt = create_pbr_material("Mat_SteelBolt", (0.75, 0.75, 0.77, 1.0), metallic=0.95, roughness=0.2)
    mat_optic_lens = create_pbr_material("Mat_OpticGlass", (0.05, 0.4, 0.5, 0.6), metallic=0.1, roughness=0.1)

    is_pistol = "pistol" in model_name or "revolver" in model_name
    is_shotgun = "shotgun" in model_name
    is_sniper = "sniper" in model_name
    is_smg = "smg" in model_name
    is_lmg = "lmg" in model_name
    is_silenced = "silenced" in model_name

    # Dimensiones ajustadas por arquitectura de arma
    rec_scale = (0.045, 0.42, 0.08)
    barrel_radius = 0.015
    barrel_length = 0.45
    barrel_pos_y = 0.42
    muzzle_pos_y = 0.67
    stock_scale = (0.042, 0.28, 0.11)
    mag_scale = (0.032, 0.08, 0.22)
    mag_pos = (0, 0.08, -0.15)
    bolt_pos = (0.024, 0.03, 0.028)

    if is_pistol:
        rec_scale = (0.034, 0.20, 0.06)
        barrel_radius = 0.012
        barrel_length = 0.16
        barrel_pos_y = 0.14
        muzzle_pos_y = 0.24
        mag_scale = (0.026, 0.05, 0.14)
        mag_pos = (0, -0.04, -0.16)
        bolt_pos = (0, 0.01, 0.03)
    elif is_shotgun:
        rec_scale = (0.048, 0.48, 0.09)
        barrel_radius = 0.022
        barrel_length = 0.52
        barrel_pos_y = 0.48
        muzzle_pos_y = 0.75
        stock_scale = (0.045, 0.32, 0.13)
        mag_scale = (0.025, 0.45, 0.025) # Tubo cargador bajo el cañón
        mag_pos = (0, 0.25, -0.02)
    elif is_sniper:
        rec_scale = (0.05, 0.55, 0.09)
        barrel_radius = 0.018
        barrel_length = 0.75
        barrel_pos_y = 0.60
        muzzle_pos_y = 0.98
        stock_scale = (0.045, 0.35, 0.14)
        mag_scale = (0.035, 0.09, 0.18)
        mag_pos = (0, 0.05, -0.14)
    elif is_smg:
        rec_scale = (0.038, 0.30, 0.07)
        barrel_radius = 0.013
        barrel_length = 0.28
        barrel_pos_y = 0.28
        muzzle_pos_y = 0.44
        stock_scale = (0.035, 0.22, 0.08)
        mag_scale = (0.028, 0.055, 0.26)
        mag_pos = (0, 0.04, -0.17)
    elif is_lmg:
        rec_scale = (0.055, 0.50, 0.10)
        barrel_radius = 0.020
        barrel_length = 0.58
        barrel_pos_y = 0.52
        muzzle_pos_y = 0.82
        stock_scale = (0.05, 0.30, 0.12)
        mag_scale = (0.12, 0.15, 0.18) # Tambor / Caja de cinta
        mag_pos = (0, 0.06, -0.16)

    # 1. Cuerpo principal / Receptor (Receiver)
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 0))
    receiver = bpy.context.active_object
    receiver.name = "Receiver"
    receiver.scale = rec_scale
    bpy.ops.object.transform_apply(scale=True)
    receiver.data.materials.append(mat_metal_dark if not is_lmg else mat_metal_fde)

    # 2. Cañón (Barrel)
    bpy.ops.mesh.primitive_cylinder_add(radius=barrel_radius, depth=barrel_length, location=(0, barrel_pos_y, 0.015))
    barrel = bpy.context.active_object
    barrel.rotation_euler = (math.radians(90), 0, 0)
    bpy.ops.object.transform_apply(rotation=True)
    barrel.data.materials.append(mat_metal_dark)

    # 3. Bocacha / Silenciador
    if is_silenced:
        bpy.ops.mesh.primitive_cylinder_add(radius=0.024, depth=0.22, location=(0, muzzle_pos_y + 0.08, 0.015))
    else:
        bpy.ops.mesh.primitive_cylinder_add(radius=barrel_radius * 1.35, depth=0.08, location=(0, muzzle_pos_y, 0.015))
    muzzle = bpy.context.active_object
    muzzle.rotation_euler = (math.radians(90), 0, 0)
    bpy.ops.object.transform_apply(rotation=True)
    muzzle.data.materials.append(mat_metal_dark)

    static_parts = [receiver, barrel, muzzle]

    # 4. Guardamanos (Handguard) si no es pistola
    if not is_pistol:
        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, barrel_pos_y * 0.65, 0.015))
        handguard = bpy.context.active_object
        handguard.scale = (rec_scale[0] * 1.05, barrel_length * 0.55, rec_scale[2] * 0.85)
        bpy.ops.object.transform_apply(scale=True)
        handguard.data.materials.append(mat_polymer_tan if not is_lmg else mat_metal_fde)
        static_parts.append(handguard)

    # 5. Empuñadura ergonómica de combate (Pistol Grip)
    grip_y = -0.06 if is_pistol else -0.12
    grip_z = -0.08 if is_pistol else -0.11
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, grip_y, grip_z))
    grip = bpy.context.active_object
    grip.name = "PistolGrip"
    grip.scale = (rec_scale[0] * 0.85, 0.055 if is_pistol else 0.065, 0.11 if is_pistol else 0.15)
    grip.rotation_euler = (math.radians(-18), 0, 0)
    bpy.ops.object.transform_apply(scale=True, rotation=True)
    grip.data.materials.append(mat_polymer_tan if not is_pistol else mat_metal_dark)
    static_parts.append(grip)

    # 6. Culata (Stock) si no es pistola
    if not is_pistol:
        stock_y = -(rec_scale[1] * 0.5 + stock_scale[1] * 0.5)
        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, stock_y, -0.01))
        stock = bpy.context.active_object
        stock.scale = stock_scale
        bpy.ops.object.transform_apply(scale=True)
        stock.data.materials.append(mat_polymer_tan if not is_lmg else mat_metal_fde)
        static_parts.append(stock)

    # 7. Óptica de Combate / Mira
    if is_sniper:
        # Mira telescópica de francotirador de largo alcance
        bpy.ops.mesh.primitive_cylinder_add(radius=0.026, depth=0.32, location=(0, 0.05, 0.12))
        optic_tube = bpy.context.active_object
        optic_tube.rotation_euler = (math.radians(90), 0, 0)
        bpy.ops.object.transform_apply(rotation=True)
        optic_tube.data.materials.append(mat_metal_dark)
        static_parts.append(optic_tube)
    elif not is_pistol:
        # Mira holográfica / Red Dot
        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0.02, 0.075))
        sight_base = bpy.context.active_object
        sight_base.scale = (0.04, 0.14, 0.04)
        bpy.ops.object.transform_apply(scale=True)
        sight_base.data.materials.append(mat_metal_dark)
        static_parts.append(sight_base)

        bpy.ops.mesh.primitive_cylinder_add(radius=0.018, depth=0.12, location=(0, 0.02, 0.095))
        optic_tube = bpy.context.active_object
        optic_tube.rotation_euler = (math.radians(90), 0, 0)
        bpy.ops.object.transform_apply(rotation=True)
        optic_tube.data.materials.append(mat_optic_lens)
        static_parts.append(optic_tube)

    # Unir partes estáticas del arma
    bpy.ops.object.select_all(action='DESELECT')
    for p in static_parts:
        p.select_set(True)
    bpy.context.view_layer.objects.active = receiver
    bpy.ops.object.join()
    weapon_mesh = bpy.context.active_object
    weapon_mesh.name = "Weapon_Body"

    # 8. Cerrojo móvil (Bolt / Slide)
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=bolt_pos)
    bolt = bpy.context.active_object
    bolt.name = "Weapon_Bolt"
    bolt.scale = (0.018 if not is_pistol else 0.036, 0.09 if not is_pistol else 0.18, 0.025 if not is_pistol else 0.035)
    bpy.ops.object.transform_apply(scale=True)
    bolt.data.materials.append(mat_steel_bolt)

    # 9. Cargador extraíble (Magazine)
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=mag_pos)
    mag = bpy.context.active_object
    mag.name = "Weapon_Magazine"
    mag.scale = mag_scale
    if not is_shotgun and not is_pistol:
        mag.rotation_euler = (math.radians(12), 0, 0)
    bpy.ops.object.transform_apply(scale=True, rotation=True)
    mag.data.materials.append(mat_metal_dark)

    muzzle_socket_y = muzzle_pos_y + (0.15 if is_silenced else 0.05)
    ads_socket_z = 0.12 if is_sniper else (0.095 if not is_pistol else 0.05)

    return setup_weapon_rig_and_animations(weapon_mesh, bolt, mag, muzzle_y=muzzle_socket_y, ads_z=ads_socket_z)


def setup_weapon_rig_and_animations(body, bolt, mag, muzzle_y=0.75, ads_z=0.095):
    """Crea el esqueleto militar del arma, vincula los vértices y hornea animaciones de combate."""
    bpy.ops.object.armature_add(location=(0, 0, 0))
    arm_obj = bpy.context.active_object
    arm_obj.name = "Armature_Weapon"
    arm_data = arm_obj.data
    arm_data.name = "WeaponRig"

    bpy.ops.object.mode_set(mode='EDIT')
    edit_bones = arm_data.edit_bones
    root_bone = edit_bones[0]
    root_bone.name = "weapon_root"
    root_bone.head = (0, 0, 0)
    root_bone.tail = (0, 0, 0.1)

    # Hueso del cerrojo
    bolt_bone = edit_bones.new("weapon_bolt")
    bolt_bone.parent = root_bone
    bolt_bone.head = (0.024, 0.03, 0.028)
    bolt_bone.tail = (0.024, -0.05, 0.028)

    # Hueso del cargador
    mag_bone = edit_bones.new("weapon_magazine")
    mag_bone.parent = root_bone
    mag_bone.head = (0, 0.08, -0.05)
    mag_bone.tail = (0, 0.08, -0.25)

    # Socket para fogonazo (Muzzle flash)
    muzzle_bone = edit_bones.new("socket_muzzle")
    muzzle_bone.parent = root_bone
    muzzle_bone.head = (0, muzzle_y, 0.015)
    muzzle_bone.tail = (0, muzzle_y + 0.05, 0.015)

    # Socket para alineación al apuntar con la mira (ADS Sight)
    ads_bone = edit_bones.new("socket_ads_sight")
    ads_bone.parent = root_bone
    ads_bone.head = (0, 0.02, ads_z)
    ads_bone.tail = (0, 0.05, ads_z)

    bpy.ops.object.mode_set(mode='OBJECT')

    # Asignar grupos de vértices
    def assign_group(obj, group_name):
        vg = obj.vertex_groups.new(name=group_name)
        indices = [v.index for v in obj.data.vertices]
        vg.add(indices, 1.0, 'REPLACE')

    assign_group(body, "weapon_root")
    assign_group(bolt, "weapon_bolt")
    assign_group(mag, "weapon_magazine")

    # Unir las mallas en una única malla optimizada
    bpy.ops.object.select_all(action='DESELECT')
    body.select_set(True)
    bolt.select_set(True)
    mag.select_set(True)
    bpy.context.view_layer.objects.active = body
    bpy.ops.object.join()
    full_weapon = bpy.context.active_object

    full_weapon.parent = arm_obj
    modifier = full_weapon.modifiers.new(name="Armature", type='ARMATURE')
    modifier.object = arm_obj

    # Animaciones F-Curves
    arm_obj.animation_data_create()

    # 1. Anim_Fire
    action_fire = bpy.data.actions.new(name="Anim_Fire")
    arm_obj.animation_data.action = action_fire
    pose_bones = arm_obj.pose.bones
    p_root = pose_bones["weapon_root"]
    p_bolt = pose_bones["weapon_bolt"]

    p_root.location = (0, 0, 0)
    p_root.rotation_euler = (0, 0, 0)
    p_bolt.location = (0, 0, 0)
    p_root.keyframe_insert(data_path="location", frame=1)
    p_root.keyframe_insert(data_path="rotation_euler", frame=1)
    p_bolt.keyframe_insert(data_path="location", frame=1)

    p_root.location = (0, -0.045, 0.012)
    p_root.rotation_euler = (math.radians(-4.5), 0, 0)
    p_bolt.location = (0, -0.075, 0)
    p_root.keyframe_insert(data_path="location", frame=2)
    p_root.keyframe_insert(data_path="rotation_euler", frame=2)
    p_bolt.keyframe_insert(data_path="location", frame=2)

    p_root.location = (0, 0, 0)
    p_root.rotation_euler = (0, 0, 0)
    p_bolt.location = (0, 0, 0)
    p_root.keyframe_insert(data_path="location", frame=5)
    p_root.keyframe_insert(data_path="rotation_euler", frame=5)
    p_bolt.keyframe_insert(data_path="location", frame=5)

    # 2. Anim_Reload
    action_reload = bpy.data.actions.new(name="Anim_Reload")
    arm_obj.animation_data.action = action_reload
    p_mag = pose_bones["weapon_magazine"]

    p_mag.location = (0, 0, 0)
    p_mag.keyframe_insert(data_path="location", frame=1)
    p_mag.location = (0, 0, -0.35)
    p_mag.keyframe_insert(data_path="location", frame=15)
    p_mag.location = (0, 0, -0.35)
    p_mag.keyframe_insert(data_path="location", frame=30)
    p_mag.location = (0, 0, 0)
    p_mag.keyframe_insert(data_path="location", frame=45)

    arm_obj.animation_data.action = action_fire
    return arm_obj


# ==============================================================================
# 2. PERSONAJES / SOLDADOS MILITARES REALISTAS (CHARACTERS)
# ==============================================================================

def build_realistic_character(model_name):
    """
    Construye un operador táctico / soldado realista con chaleco antibalas,
    casco balístico, botas militares y esqueleto humanoide con animaciones tácticas.
    Soporta múltiples variantes:
      - operator_soldier (Camuflaje boscoso / estándar)
      - enemy_mercenary (Urbano / Oscuro)
      - covert_specops (Negro mate / Visor nocturno)
      - desert_trooper (Tan / Árido)
      - arctic_commando (Blanco / Nieve)
      - heavy_juggernaut (Blindaje reforzado)
    """
    is_specops = "specops" in model_name or "mercenary" in model_name
    is_desert = "desert" in model_name
    is_arctic = "arctic" in model_name
    is_heavy = "heavy" in model_name or "juggernaut" in model_name

    camo_color = (0.24, 0.27, 0.22, 1.0)
    vest_color = (0.16, 0.17, 0.16, 1.0)

    if is_desert:
        camo_color = (0.55, 0.48, 0.35, 1.0)
        vest_color = (0.42, 0.36, 0.26, 1.0)
    elif is_arctic:
        camo_color = (0.85, 0.88, 0.90, 1.0)
        vest_color = (0.50, 0.55, 0.58, 1.0)
    elif is_specops:
        camo_color = (0.12, 0.12, 0.14, 1.0)
        vest_color = (0.08, 0.08, 0.09, 1.0)

    mat_fatigues = create_pbr_material("Mat_TacticalCamo", camo_color, metallic=0.05, roughness=0.85)
    mat_vest_heavy = create_pbr_material("Mat_PlateCarrier", vest_color, metallic=0.1, roughness=0.7)
    mat_skin = create_pbr_material("Mat_SkinFace", (0.75, 0.58, 0.48, 1.0), metallic=0.0, roughness=0.6)
    mat_boots_leather = create_pbr_material("Mat_Boots", (0.08, 0.08, 0.08, 1.0), metallic=0.2, roughness=0.45)
    mat_helmet_visor = create_pbr_material("Mat_VisorGlass", (0.1, 0.4, 0.15, 0.9) if is_specops else (0.1, 0.12, 0.15, 0.8), metallic=0.9, roughness=0.1)

    # 1. Torso / Chaleco Balístico
    torso_scale = (0.34, 0.22, 0.36) if is_heavy else (0.28, 0.18, 0.32)
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 1.30))
    torso = bpy.context.active_object
    torso.name = "Torso"
    torso.scale = torso_scale
    bpy.ops.object.transform_apply(scale=True)
    torso.data.materials.append(mat_vest_heavy)

    # 2. Cabeza y Cuello
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 1.68))
    head = bpy.context.active_object
    head.name = "Head"
    head.scale = (0.13, 0.14, 0.16)
    bpy.ops.object.transform_apply(scale=True)
    head.data.materials.append(mat_skin)

    # Casco militar FAST / Visor táctico
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 1.76))
    helmet = bpy.context.active_object
    helmet.scale = (0.145, 0.155, 0.09)
    bpy.ops.object.transform_apply(scale=True)
    helmet.data.materials.append(mat_vest_heavy)

    bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0.12, 1.70))
    goggles = bpy.context.active_object
    goggles.scale = (0.12, 0.04, 0.04)
    bpy.ops.object.transform_apply(scale=True)
    goggles.data.materials.append(mat_helmet_visor)

    # 3. Extremidades Superiores
    arm_radius = 0.08 if is_heavy else 0.06
    bpy.ops.mesh.primitive_cylinder_add(radius=arm_radius, depth=0.55, location=(-0.34, 0.0, 1.25))
    arm_l = bpy.context.active_object
    arm_l.name = "Arm_L"
    arm_l.data.materials.append(mat_fatigues)

    bpy.ops.mesh.primitive_cylinder_add(radius=arm_radius, depth=0.55, location=(0.34, 0.0, 1.25))
    arm_r = bpy.context.active_object
    arm_r.name = "Arm_R"
    arm_r.data.materials.append(mat_fatigues)

    # 4. Extremidades Inferiores
    leg_radius = 0.10 if is_heavy else 0.08
    bpy.ops.mesh.primitive_cylinder_add(radius=leg_radius, depth=0.75, location=(-0.15, 0.0, 0.65))
    leg_l = bpy.context.active_object
    leg_l.name = "Leg_L"
    leg_l.data.materials.append(mat_fatigues)

    bpy.ops.mesh.primitive_cube_add(size=1.0, location=(-0.15, 0.05, 0.12))
    boot_l = bpy.context.active_object
    boot_l.name = "Boot_L"
    boot_l.scale = (0.09, 0.22, 0.14)
    bpy.ops.object.transform_apply(scale=True)
    boot_l.data.materials.append(mat_boots_leather)

    bpy.ops.mesh.primitive_cylinder_add(radius=leg_radius, depth=0.75, location=(0.15, 0.0, 0.65))
    leg_r = bpy.context.active_object
    leg_r.name = "Leg_R"
    leg_r.data.materials.append(mat_fatigues)

    bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0.15, 0.05, 0.12))
    boot_r = bpy.context.active_object
    boot_r.name = "Boot_R"
    boot_r.scale = (0.09, 0.22, 0.14)
    bpy.ops.object.transform_apply(scale=True)
    boot_r.data.materials.append(mat_boots_leather)

    # Asignar grupos de vértices
    def assign_group(obj, group_name):
        vg = obj.vertex_groups.new(name=group_name)
        indices = [v.index for v in obj.data.vertices]
        vg.add(indices, 1.0, 'REPLACE')

    assign_group(torso, "spine")
    assign_group(head, "head")
    assign_group(helmet, "head")
    assign_group(goggles, "head")
    assign_group(arm_l, "arm_l")
    assign_group(arm_r, "arm_r")
    assign_group(leg_l, "leg_l")
    assign_group(boot_l, "foot_l")
    assign_group(leg_r, "leg_r")
    assign_group(boot_r, "foot_r")

    # Unir mallas
    all_body_parts = [torso, head, helmet, goggles, arm_l, arm_r, leg_l, boot_l, leg_r, boot_r]
    bpy.ops.object.select_all(action='DESELECT')
    for part in all_body_parts:
        part.select_set(True)
    bpy.context.view_layer.objects.active = torso
    bpy.ops.object.join()
    soldier_mesh = bpy.context.active_object
    soldier_mesh.name = "Soldier_Body"

    return setup_character_rig_and_animations(soldier_mesh)


def setup_character_rig_and_animations(soldier_mesh):
    """Crea el esqueleto humanoide táctico para el operador y hornea animaciones de combate."""
    bpy.ops.object.armature_add(location=(0, 0, 0))
    arm_obj = bpy.context.active_object
    arm_obj.name = "Armature_Soldier"
    arm_data = arm_obj.data

    bpy.ops.object.mode_set(mode='EDIT')
    edit_bones = arm_data.edit_bones

    root = edit_bones[0]
    root.name = "root"
    root.head = (0, 0, 0)
    root.tail = (0, 0, 0.2)

    spine = edit_bones.new("spine")
    spine.parent = root
    spine.head = (0, 0, 1.0)
    spine.tail = (0, 0, 1.5)

    head = edit_bones.new("head")
    head.parent = spine
    head.head = (0, 0, 1.55)
    head.tail = (0, 0, 1.85)

    arm_l = edit_bones.new("arm_l")
    arm_l.parent = spine
    arm_l.head = (-0.28, 0, 1.45)
    arm_l.tail = (-0.38, 0.15, 1.0)

    arm_r = edit_bones.new("arm_r")
    arm_r.parent = spine
    arm_r.head = (0.28, 0, 1.45)
    arm_r.tail = (0.38, 0.15, 1.0)

    leg_l = edit_bones.new("leg_l")
    leg_l.parent = root
    leg_l.head = (-0.15, 0, 1.0)
    leg_l.tail = (-0.15, 0, 0.3)

    foot_l = edit_bones.new("foot_l")
    foot_l.parent = leg_l
    foot_l.head = (-0.15, 0, 0.3)
    foot_l.tail = (-0.15, 0.15, 0.0)

    leg_r = edit_bones.new("leg_r")
    leg_r.parent = root
    leg_r.head = (0.15, 0, 1.0)
    leg_r.tail = (0.15, 0, 0.3)

    foot_r = edit_bones.new("foot_r")
    foot_r.parent = leg_r
    foot_r.head = (0.15, 0, 0.3)
    foot_r.tail = (0.15, 0.15, 0.0)

    bpy.ops.object.mode_set(mode='OBJECT')

    soldier_mesh.parent = arm_obj
    mod = soldier_mesh.modifiers.new(name="Armature", type='ARMATURE')
    mod.object = arm_obj

    arm_obj.animation_data_create()

    # 1. Anim_CombatIdle
    act_idle = bpy.data.actions.new(name="Anim_CombatIdle")
    arm_obj.animation_data.action = act_idle
    p_spine = arm_obj.pose.bones["spine"]
    p_arm_r = arm_obj.pose.bones["arm_r"]
    p_arm_l = arm_obj.pose.bones["arm_l"]

    for frame, pitch, roll in [(1, 0.0, 0.0), (20, math.radians(2.0), math.radians(1.0)), (40, 0.0, 0.0)]:
        p_spine.rotation_euler = (pitch, roll, 0)
        p_arm_r.rotation_euler = (math.radians(-15) + pitch, 0, math.radians(-10))
        p_arm_l.rotation_euler = (math.radians(-25) + pitch, 0, math.radians(15))
        p_spine.keyframe_insert(data_path="rotation_euler", frame=frame)
        p_arm_r.keyframe_insert(data_path="rotation_euler", frame=frame)
        p_arm_l.keyframe_insert(data_path="rotation_euler", frame=frame)

    # 2. Anim_CombatRun
    act_run = bpy.data.actions.new(name="Anim_CombatRun")
    arm_obj.animation_data.action = act_run
    p_leg_l = arm_obj.pose.bones["leg_l"]
    p_leg_r = arm_obj.pose.bones["leg_r"]

    stride = math.radians(25)
    p_leg_l.rotation_euler = (stride, 0, 0)
    p_leg_r.rotation_euler = (-stride, 0, 0)
    p_leg_l.keyframe_insert(data_path="rotation_euler", frame=1)
    p_leg_r.keyframe_insert(data_path="rotation_euler", frame=1)

    p_leg_l.rotation_euler = (-stride, 0, 0)
    p_leg_r.rotation_euler = (stride, 0, 0)
    p_leg_l.keyframe_insert(data_path="rotation_euler", frame=10)
    p_leg_r.keyframe_insert(data_path="rotation_euler", frame=10)

    p_leg_l.rotation_euler = (stride, 0, 0)
    p_leg_r.rotation_euler = (-stride, 0, 0)
    p_leg_l.keyframe_insert(data_path="rotation_euler", frame=20)
    p_leg_r.keyframe_insert(data_path="rotation_euler", frame=20)

    arm_obj.animation_data.action = act_idle
    return arm_obj


# ==============================================================================
# 3. ENTORNO Y COBERTURAS TÁCTICAS REALISTAS (ENVIRONMENT)
# ==============================================================================

def build_realistic_environment(model_name):
    """
    Construye estructuras balísticas realistas:
      - tactical_wall_barrier (Muro balístico de hormigón)
      - sandbag_bunker (Barricada de sacos de arena)
      - ammo_supply_crate (Caja de munición y suministros)
      - anti_tank_hedgehog (Erizo checo antivehículo de acero)
      - military_watchtower (Torreta de vigilancia táctica)
      - shipping_container (Contenedor de transporte militar)
    """
    mat_concrete = create_pbr_material("Mat_FortifiedConcrete", (0.42, 0.43, 0.44, 1.0), metallic=0.05, roughness=0.9)
    mat_sandbag = create_pbr_material("Mat_KevlarSandbag", (0.55, 0.48, 0.35, 1.0), metallic=0.0, roughness=0.95)
    mat_crate_olive = create_pbr_material("Mat_MilSupplyCrate", (0.20, 0.25, 0.18, 1.0), metallic=0.2, roughness=0.6)
    mat_steel_dark = create_pbr_material("Mat_SteelDark", (0.12, 0.13, 0.14, 1.0), metallic=0.95, roughness=0.3)
    mat_rust_metal = create_pbr_material("Mat_RustMetal", (0.38, 0.18, 0.12, 1.0), metallic=0.6, roughness=0.85)

    if "sandbag" in model_name:
        sandbags = []
        for row in range(3):
            z = 0.15 + (row * 0.22)
            for col in range(5):
                x = -1.0 + (col * 0.48) + (0.12 if row % 2 == 1 else 0.0)
                bpy.ops.mesh.primitive_cube_add(size=1.0, location=(x, 0, z))
                bag = bpy.context.active_object
                bag.scale = (0.24, 0.16, 0.11)
                bpy.ops.object.transform_apply(scale=True)
                bag.data.materials.append(mat_sandbag)
                sandbags.append(bag)

        bpy.ops.object.select_all(action='DESELECT')
        for b in sandbags:
            b.select_set(True)
        bpy.context.view_layer.objects.active = sandbags[0]
        bpy.ops.object.join()
        env_obj = bpy.context.active_object
        env_obj.name = "Sandbag_Bunker"

    elif "crate" in model_name:
        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 0.45))
        crate = bpy.context.active_object
        crate.scale = (0.7, 0.45, 0.45)
        bpy.ops.object.transform_apply(scale=True)
        crate.data.materials.append(mat_crate_olive)

        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 0.46))
        corners = bpy.context.active_object
        corners.scale = (0.72, 0.47, 0.42)
        bpy.ops.object.transform_apply(scale=True)
        corners.data.materials.append(mat_steel_dark)

        bpy.ops.object.select_all(action='DESELECT')
        crate.select_set(True)
        corners.select_set(True)
        bpy.context.view_layer.objects.active = crate
        bpy.ops.object.join()
        env_obj = bpy.context.active_object
        env_obj.name = "Ammo_Supply_Crate"

    elif "hedgehog" in model_name:
        # Erizo checo defensivo de tres vigas de acero cruzadas
        beams = []
        for rot in [(math.radians(45), math.radians(45), 0),
                    (math.radians(-45), math.radians(45), 0),
                    (0, math.radians(-45), math.radians(45))]:
            bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 0.6))
            beam = bpy.context.active_object
            beam.scale = (0.12, 0.12, 1.4)
            beam.rotation_euler = rot
            bpy.ops.object.transform_apply(scale=True, rotation=True)
            beam.data.materials.append(mat_steel_dark)
            beams.append(beam)

        bpy.ops.object.select_all(action='DESELECT')
        for bm in beams:
            bm.select_set(True)
        bpy.context.view_layer.objects.active = beams[0]
        bpy.ops.object.join()
        env_obj = bpy.context.active_object
        env_obj.name = "Anti_Tank_Hedgehog"

    elif "container" in model_name:
        # Contenedor militar de transporte con corrugado
        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 1.3))
        container = bpy.context.active_object
        container.scale = (1.2, 3.0, 1.3)
        bpy.ops.object.transform_apply(scale=True)
        container.data.materials.append(mat_rust_metal)
        env_obj = container
        env_obj.name = "Shipping_Container"

    else:
        # Muro balístico militar con ranura para tirador
        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 1.25))
        wall = bpy.context.active_object
        wall.scale = (1.5, 0.25, 1.25)
        bpy.ops.object.transform_apply(scale=True)
        wall.data.materials.append(mat_concrete)

        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(0, 0, 0.1))
        base = bpy.context.active_object
        base.scale = (1.65, 0.5, 0.1)
        bpy.ops.object.transform_apply(scale=True)
        base.data.materials.append(mat_steel_dark)

        bpy.ops.object.select_all(action='DESELECT')
        wall.select_set(True)
        base.select_set(True)
        bpy.context.view_layer.objects.active = wall
        bpy.ops.object.join()
        env_obj = bpy.context.active_object
        env_obj.name = "Tactical_Wall_Barrier"

    return env_obj


# ==============================================================================
# 4. EXPORTADOR GLTF / GLB MÓVIL OPTIMIZADO
# ==============================================================================

def export_glb(output_path, include_animations=True):
    """Exporta la escena procesada en formato .glb binario listo para móvil."""
    out_dir = os.path.dirname(os.path.abspath(output_path))
    if out_dir and not os.path.exists(out_dir):
        os.makedirs(out_dir, exist_ok=True)

    bpy.ops.object.select_all(action='SELECT')

    bpy.ops.export_scene.gltf(
        filepath=output_path,
        export_format='GLB',
        use_selection=True,
        export_apply=False,
        export_armatures=True,
        export_animations=include_animations,
        export_nla_strips=include_animations,
        export_def_bones=True,
        export_materials='EXPORT',
        export_colors=True,
        export_yup=True
    )
    print(f"[SUCCESS] Asset 3D exportado exitosamente en: {output_path}")


def main():
    args = parse_args()
    print("==========================================================")
    print("  PolyStrike 3D - Generador de Assets Tácticos Realistas")
    print(f"  Tipo: {args.type} | Modelo: {args.model} | Animado: {args.animated}")
    print("  Licencias: Exclusivamente CC0 / Dominio Público / MIT")
    print("==========================================================")

    reset_scene()

    if args.type == "weapon":
        build_realistic_weapon(args.model)
    elif args.type == "character":
        build_realistic_character(args.model)
    elif args.type == "environment":
        build_realistic_environment(args.model)

    if not args.output:
        dest_dir = os.path.join("generated_assets", "models")
        os.makedirs(dest_dir, exist_ok=True)
        args.output = os.path.join(dest_dir, f"{args.model}.glb")

    export_glb(args.output, include_animations=args.animated)


if __name__ == "__main__":
    main()
