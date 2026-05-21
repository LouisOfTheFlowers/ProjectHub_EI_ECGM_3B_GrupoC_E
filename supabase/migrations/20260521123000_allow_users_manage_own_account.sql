-- Allow authenticated users to update their own public email and delete their own account.

create or replace function public.user_update_own_email(p_new_email text)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  current_public_id integer;
begin
  current_public_id := public.current_user_public_id();

  if current_public_id is null then
    raise exception 'Utilizador nao encontrado.';
  end if;

  if p_new_email is null or length(trim(p_new_email)) = 0 then
    raise exception 'Email invalido.';
  end if;

  if exists (
    select 1
    from public.users
    where lower(email) = lower(trim(p_new_email))
      and id <> current_public_id
  ) then
    raise exception 'Ja existe uma conta com esse email.';
  end if;

  update public.users
  set email = trim(p_new_email)
  where id = current_public_id;
end;
$$;

grant execute on function public.user_update_own_email(text) to authenticated;

create or replace function public.user_delete_own_account()
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  target_user_id integer;
  target_email text;
begin
  target_user_id := public.current_user_public_id();

  if target_user_id is null then
    raise exception 'Utilizador nao encontrado.';
  end if;

  select email
  into target_email
  from public.users
  where id = target_user_id;

  delete from public.observacao_fotos
  where observacao_id in (
    select observacoes.id
    from public.observacoes
    inner join public.registos_tarefa
      on registos_tarefa.id = observacoes.registo_id
    where registos_tarefa.user_id = target_user_id
  );

  delete from public.observacoes
  where registo_id in (
    select id
    from public.registos_tarefa
    where user_id = target_user_id
  );

  delete from public.registos_tarefa
  where user_id = target_user_id;

  delete from public.avaliacoes
  where user_id = target_user_id;

  delete from public.tarefa_users
  where user_id = target_user_id;

  delete from public.projeto_users
  where user_id = target_user_id;

  update public.projetos
  set gestor_id = null
  where gestor_id = target_user_id;

  delete from public.users
  where id = target_user_id;

  delete from auth.users
  where email = target_email;
end;
$$;

grant execute on function public.user_delete_own_account() to authenticated;
