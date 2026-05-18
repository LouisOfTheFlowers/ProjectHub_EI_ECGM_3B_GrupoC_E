-- Keep public.users in sync with Supabase Auth email confirmation.
-- Run this in Supabase SQL Editor, or through the Supabase CLI.

alter table public.users
alter column password set default 'SUPABASE_AUTH';

alter table public.users
alter column password drop not null;

create unique index if not exists users_email_unique
on public.users (email);

drop policy if exists "Users can read own profile" on public.users;
drop policy if exists "Users can update own profile" on public.users;

create policy "Users can read own profile"
on public.users
for select
to authenticated
using (email = auth.jwt() ->> 'email');

create policy "Users can update own profile"
on public.users
for update
to authenticated
using (email = auth.jwt() ->> 'email')
with check (email = auth.jwt() ->> 'email');

create or replace function public.handle_auth_user_profile()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.users (
    nome,
    username,
    email,
    password,
    role,
    status
  )
  values (
    coalesce(new.raw_user_meta_data ->> 'nome', split_part(new.email, '@', 1)),
    coalesce(new.raw_user_meta_data ->> 'username', split_part(new.email, '@', 1)),
    new.email,
    'SUPABASE_AUTH',
    'UTILIZADOR',
    case
      when new.email_confirmed_at is null then 'PENDENTE'
      else 'ATIVO'
    end
  )
  on conflict (email) do update
  set
    nome = coalesce(excluded.nome, public.users.nome),
    username = coalesce(excluded.username, public.users.username),
    status = case
      when new.email_confirmed_at is null then public.users.status
      else 'ATIVO'
    end;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created_or_updated on auth.users;

create trigger on_auth_user_created_or_updated
after insert or update of email_confirmed_at, raw_user_meta_data
on auth.users
for each row
execute function public.handle_auth_user_profile();

insert into public.users (
  nome,
  username,
  email,
  password,
  role,
  status
)
select
  coalesce(au.raw_user_meta_data ->> 'nome', split_part(au.email, '@', 1)),
  coalesce(au.raw_user_meta_data ->> 'username', split_part(au.email, '@', 1)),
  au.email,
  'SUPABASE_AUTH',
  'UTILIZADOR',
  case
    when au.email_confirmed_at is null then 'PENDENTE'
    else 'ATIVO'
  end
from auth.users au
where au.email is not null
on conflict (email) do update
set status = excluded.status;
