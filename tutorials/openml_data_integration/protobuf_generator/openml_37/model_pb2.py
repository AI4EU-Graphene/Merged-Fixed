# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: model.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='model.proto',
  package='',
  syntax='proto3',
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n\x0bmodel.proto\"\x07\n\x05\x45mpty\"\x88\x01\n\x08\x46\x65\x61tures\x12\x0c\n\x04Preg\x18\x01 \x01(\r\x12\x0c\n\x04Plas\x18\x02 \x01(\r\x12\x0c\n\x04Pres\x18\x03 \x01(\r\x12\x0c\n\x04Skin\x18\x04 \x01(\r\x12\x0c\n\x04Insu\x18\x05 \x01(\x01\x12\x0c\n\x04Mass\x18\x06 \x01(\x01\x12\x0c\n\x04Pedi\x18\x07 \x01(\x01\x12\x0b\n\x03\x41ge\x18\x08 \x01(\r\x12\r\n\x05\x43lass\x18\t \x01(\t21\n\x0cget_next_row\x12!\n\x0cget_next_row\x12\x06.Empty\x1a\t.Featuresb\x06proto3'
)




_EMPTY = _descriptor.Descriptor(
  name='Empty',
  full_name='Empty',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=15,
  serialized_end=22,
)


_FEATURES = _descriptor.Descriptor(
  name='Features',
  full_name='Features',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='Preg', full_name='Features.Preg', index=0,
      number=1, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='Plas', full_name='Features.Plas', index=1,
      number=2, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='Pres', full_name='Features.Pres', index=2,
      number=3, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='Skin', full_name='Features.Skin', index=3,
      number=4, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='Insu', full_name='Features.Insu', index=4,
      number=5, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='Mass', full_name='Features.Mass', index=5,
      number=6, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='Pedi', full_name='Features.Pedi', index=6,
      number=7, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='Age', full_name='Features.Age', index=7,
      number=8, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='Class', full_name='Features.Class', index=8,
      number=9, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=25,
  serialized_end=161,
)

DESCRIPTOR.message_types_by_name['Empty'] = _EMPTY
DESCRIPTOR.message_types_by_name['Features'] = _FEATURES
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

Empty = _reflection.GeneratedProtocolMessageType('Empty', (_message.Message,), {
  'DESCRIPTOR' : _EMPTY,
  '__module__' : 'model_pb2'
  # @@protoc_insertion_point(class_scope:Empty)
  })
_sym_db.RegisterMessage(Empty)

Features = _reflection.GeneratedProtocolMessageType('Features', (_message.Message,), {
  'DESCRIPTOR' : _FEATURES,
  '__module__' : 'model_pb2'
  # @@protoc_insertion_point(class_scope:Features)
  })
_sym_db.RegisterMessage(Features)



_GET_NEXT_ROW = _descriptor.ServiceDescriptor(
  name='get_next_row',
  full_name='get_next_row',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_start=163,
  serialized_end=212,
  methods=[
  _descriptor.MethodDescriptor(
    name='get_next_row',
    full_name='get_next_row.get_next_row',
    index=0,
    containing_service=None,
    input_type=_EMPTY,
    output_type=_FEATURES,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
])
_sym_db.RegisterServiceDescriptor(_GET_NEXT_ROW)

DESCRIPTOR.services_by_name['get_next_row'] = _GET_NEXT_ROW

# @@protoc_insertion_point(module_scope)
